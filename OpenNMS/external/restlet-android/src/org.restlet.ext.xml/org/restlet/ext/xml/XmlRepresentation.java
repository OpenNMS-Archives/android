/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Representation based on an XML document. It knows how to evaluate XPath
 * expressions and how to manage a namespace context. This class also offers
 * convenient methods to validate the document against a specified XML scheme.
 * 
 * @author Jerome Louvel
 */
public abstract class XmlRepresentation extends WriterRepresentation
{

    /**
     * Appends the text content of a given node and its descendants to the given
     * buffer.
     * 
     * @param node
     *            The node.
     * @param sb
     *            The buffer.
     */
    private static void appendTextContent(Node node, StringBuilder sb) {
        switch (node.getNodeType()) {
        case Node.TEXT_NODE:
            sb.append(node.getNodeValue());
            break;
        case Node.CDATA_SECTION_NODE:
            sb.append(node.getNodeValue());
            break;
        case Node.COMMENT_NODE:
            sb.append(node.getNodeValue());
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            sb.append(node.getNodeValue());
            break;
        case Node.ENTITY_REFERENCE_NODE:
            if (node.getNodeName().startsWith("#")) {
                int ch = Integer.parseInt(node.getNodeName().substring(1));
                sb.append((char) ch);
            }
            break;
        case Node.ELEMENT_NODE:
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                appendTextContent(node.getChildNodes().item(i), sb);
            }
            break;
        case Node.ATTRIBUTE_NODE:
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                appendTextContent(node.getChildNodes().item(i), sb);
            }
            break;
        case Node.ENTITY_NODE:
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                appendTextContent(node.getChildNodes().item(i), sb);
            }
            break;
        case Node.DOCUMENT_FRAGMENT_NODE:
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                appendTextContent(node.getChildNodes().item(i), sb);
            }
            break;
        default:
            break;
        }
    }



    /**
     * Returns the schema URI for the current schema media type.
     * 
     * @return The schema URI.
     */
    private static String getSchemaLanguageUri(
            Representation schemaRepresentation) {
        String result = null;

        if (schemaRepresentation != null) {
            if (MediaType.APPLICATION_W3C_SCHEMA.equals(schemaRepresentation
                    .getMediaType())) {
                result = XMLConstants.W3C_XML_SCHEMA_NS_URI;
            } else if (MediaType.APPLICATION_RELAXNG_COMPACT
                    .equals(schemaRepresentation.getMediaType())) {
                result = XMLConstants.RELAXNG_NS_URI;
            } else if (MediaType.APPLICATION_RELAXNG_XML
                    .equals(schemaRepresentation.getMediaType())) {
                result = XMLConstants.RELAXNG_NS_URI;
            }
        }

        return result;
    }

    /**
     * Returns the text content of a given node and its descendants.
     * 
     * @param node
     *            The node.
     * @return The text content of a given node.
     */
    public static String getTextContent(Node node) {
        StringBuilder sb = new StringBuilder();
        appendTextContent(node, sb);
        return sb.toString();
    }

    /**
     * A SAX {@link EntityResolver} to use when resolving external entity
     * references while parsing this type of XML representations.
     * 
     * @see DocumentBuilder#setEntityResolver(EntityResolver)
     */
    private volatile EntityResolver entityResolver;

    /**
     * A SAX {@link ErrorHandler} to use for signaling SAX exceptions while
     * parsing this type of XML representations.
     * 
     * @see DocumentBuilder#setErrorHandler(ErrorHandler)
     */
    private volatile ErrorHandler errorHandler;

    /** Indicates if processing is namespace aware. */
    private volatile boolean namespaceAware;

    /** Internal map of namespaces. */
    private volatile Map<String, String> namespaces;


    /**
     * Indicates the desire for validating this type of XML representations
     * against a DTD. Note that for XML schema or Relax NG validation, use the
     * "schema" property instead.
     * 
     * @see DocumentBuilderFactory#setValidating(boolean)
     */
    private volatile boolean validatingDtd;

    /**
     * Specifies that the parser will convert CDATA nodes to text nodes and
     * append it to the adjacent (if any) text node. By default the value of
     * this is set to false.
     */
    private volatile boolean coalescing;

    /**
     * Specifies that the parser will expand entity reference nodes. By default
     * the value of this is set to true.
     */
    private volatile boolean expandingEntityRefs;

    /**
     * Indicates if the parser will ignore comments. By default the value of
     * this is set to false.
     */
    private volatile boolean ignoringComments;

    /**
     * Indicates if the parser will ignore extra white spaces in element
     * content. By default the value of this is set to false.
     */
    private volatile boolean ignoringExtraWhitespaces;

    /**
     * Indicates the desire for processing <em>XInclude</em> if found in this
     * type of XML representations. By default the value of this is set to
     * false.
     * 
     * @see DocumentBuilderFactory#setXIncludeAware(boolean)
     */
    private volatile boolean xIncludeAware;

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The representation's mediaType.
     */
    public XmlRepresentation(MediaType mediaType) {
        this(mediaType, UNKNOWN_SIZE);
    }

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The representation's mediaType.
     * @param expectedSize
     *            The expected input stream size.
     */
    public XmlRepresentation(MediaType mediaType, long expectedSize) {
        super(mediaType, expectedSize);
        this.coalescing = false;
        this.entityResolver = null;
        this.errorHandler = null;
        this.expandingEntityRefs = true;
        this.ignoringComments = false;
        this.ignoringExtraWhitespaces = false;
        this.namespaceAware = false;
        this.namespaces = null;
        this.validatingDtd = false;
        this.xIncludeAware = false;
    }



    /**
     * Returns the XML representation as a DOM document.
     * 
     * @return The DOM document.
     */
    protected Document getDocument() throws Exception {
        return getDocumentBuilder().parse(getInputSource());
    }

    /**
     * Returns a document builder properly configured.
     * 
     * @return A document builder properly configured.
     */
    protected DocumentBuilder getDocumentBuilder() throws IOException {
        DocumentBuilder result = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(isNamespaceAware());
            dbf.setValidating(isValidatingDtd());
            dbf.setCoalescing(isCoalescing());
            dbf.setExpandEntityReferences(isExpandingEntityRefs());
            dbf.setIgnoringComments(isIgnoringComments());
            dbf
                    .setIgnoringElementContentWhitespace(isIgnoringExtraWhitespaces());

            try {
                dbf.setXIncludeAware(isXIncludeAware());
            } catch (UnsupportedOperationException uoe) {
                Context.getCurrentLogger().log(Level.FINE,
                        "The JAXP parser doesn't support XInclude.", uoe);
            }


            result = dbf.newDocumentBuilder();
            result.setEntityResolver(getEntityResolver());
            result.setErrorHandler(getErrorHandler());
        } catch (ParserConfigurationException pce) {
            throw new IOException("Couldn't create the empty document: "
                    + pce.getMessage());
        }

        return result;
    }


    /**
     * Return the possibly null current SAX {@link EntityResolver}.
     * 
     * @return The possibly null current SAX {@link EntityResolver}.
     */
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    /**
     * Return the possibly null current SAX {@link ErrorHandler}.
     * 
     * @return The possibly null current SAX {@link ErrorHandler}.
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Returns the XML representation as a SAX input source.
     * 
     * @return The SAX input source.
     */
    public abstract InputSource getInputSource() throws IOException;

    /**
     * Returns the map of namespaces. Namespace prefixes are keys and URI
     * references are values.
     * 
     * @return The map of namespaces.
     */
    public Map<String, String> getNamespaces() {
        if (this.namespaces == null) {
            this.namespaces = new HashMap<String, String>();
        }

        return this.namespaces;
    }

    /**
     * {@inheritDoc
     * javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String}
     */
    public String getNamespaceURI(String prefix) {
        return (this.namespaces == null) ? null : this.namespaces.get(prefix);
    }




    /**
     * {@inheritDoc
     * javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String}
     */
    public String getPrefix(String namespaceURI) {
        String result = null;
        boolean found = false;

        for (Iterator<String> iterator = getNamespaces().keySet().iterator(); iterator
                .hasNext()
                && !found;) {
            String key = iterator.next();
            if (getNamespaces().get(key).equals(namespaceURI)) {
                found = true;
                result = key;
            }
        }

        return result;
    }

    /**
     * {@inheritDoc
     * javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String}
     */
    public Iterator<String> getPrefixes(String namespaceURI) {
        final List<String> result = new ArrayList<String>();

        for (Iterator<String> iterator = getNamespaces().keySet().iterator(); iterator
                .hasNext();) {
            String key = iterator.next();
            if (getNamespaces().get(key).equals(namespaceURI)) {
                result.add(key);
            }
        }

        return Collections.unmodifiableList(result).iterator();
    }






    /**
     * Indicates if the parser should be coalescing text. If true the parser
     * will convert CDATA nodes to text nodes and append it to the adjacent (if
     * any) text node. By default the value of this is set to false.
     * 
     * @return True if parser should be coalescing text.
     */
    public boolean isCoalescing() {
        return coalescing;
    }

    /**
     * Indicates if the parser will expand entity reference nodes. By default
     * the value of this is set to true.
     * 
     * @return True if the parser will expand entity reference nodes.
     */
    public boolean isExpandingEntityRefs() {
        return expandingEntityRefs;
    }

    /**
     * Indicates if the parser will ignore comments. By default the value of
     * this is set to false.
     * 
     * @return True if the parser will ignore comments.
     */
    public boolean isIgnoringComments() {
        return ignoringComments;
    }

    /**
     * Indicates if the parser will ignore extra white spaces in element
     * content. Note that the {@link #isValidatingDtd()} must be true when this
     * property is 'true' as validation is needed for it to work. By default the
     * value of this is set to false.
     * 
     * @return True if the parser will ignore extra white spaces.
     */
    public boolean isIgnoringExtraWhitespaces() {
        return ignoringExtraWhitespaces;
    }

    /**
     * Indicates if processing is namespace aware.
     * 
     * @return True if processing is namespace aware.
     */
    public boolean isNamespaceAware() {
        return this.namespaceAware;
    }

    /**
     * Indicates the desire for validating this type of XML representations
     * against an XML schema if one is referenced within the contents.
     * 
     * @return True if the schema-based validation is enabled.
     */
    public boolean isValidatingDtd() {
        return validatingDtd;
    }

    /**
     * Indicates the desire for processing <em>XInclude</em> if found in this
     * type of XML representations. By default the value of this is set to
     * false.
     * 
     * @return The current value of the xIncludeAware flag.
     */
    public boolean isXIncludeAware() {
        return xIncludeAware;
    }

    /**
     * Puts a new mapping between a prefix and a namespace URI.
     * 
     * @param prefix
     *            The namespace prefix.
     * @param namespaceURI
     *            The namespace URI.
     * @deprecated Use the modifiable map returned by {@link #getNamespaces()}
     */
    @Deprecated
    public void putNamespace(String prefix, String namespaceURI) {
        getNamespaces().put(prefix, namespaceURI);
    }

    /**
     * Releases the namespaces map.
     */
    @Override
    public void release() {
        if (this.namespaces != null) {
            this.namespaces.clear();
            this.namespaces = null;
        }
        super.release();
    }

    /**
     * Indicates if the parser should be coalescing text. If true the parser
     * will convert CDATA nodes to text nodes and append it to the adjacent (if
     * any) text node. By default the value of this is set to false.
     * 
     * @param coalescing
     *            True if parser should be coalescing text.
     */
    public void setCoalescing(boolean coalescing) {
        this.coalescing = coalescing;
    }

    /**
     * Set the {@link EntityResolver} to use when resolving external entity
     * references encountered in this type of XML representations.
     * 
     * @param entityResolver
     *            the {@link EntityResolver} to set.
     */
    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    /**
     * Set the {@link ErrorHandler} to use when signaling SAX event exceptions.
     * 
     * @param errorHandler
     *            the {@link ErrorHandler} to set.
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Indicates if the parser will expand entity reference nodes. By default
     * the value of this is set to true.
     * 
     * @param expandEntityRefs
     *            True if the parser will expand entity reference nodes.
     */
    public void setExpandingEntityRefs(boolean expandEntityRefs) {
        this.expandingEntityRefs = expandEntityRefs;
    }

    /**
     * Indicates if the parser will ignore comments. By default the value of
     * this is set to false.
     * 
     * @param ignoringComments
     *            True if the parser will ignore comments.
     */
    public void setIgnoringComments(boolean ignoringComments) {
        this.ignoringComments = ignoringComments;
    }

    /**
     * Indicates if the parser will ignore extra white spaces in element
     * content. Note that the {@link #setValidatingDtd(boolean)} will be invoked
     * with 'true' if setting this property to 'true' as validation is needed
     * for it to work.
     * 
     * @param ignoringExtraWhitespaces
     *            True if the parser will ignore extra white spaces in element
     *            content.
     */
    public void setIgnoringExtraWhitespaces(boolean ignoringExtraWhitespaces) {
        if (this.ignoringExtraWhitespaces != ignoringExtraWhitespaces) {
            if (ignoringExtraWhitespaces) {
                setValidatingDtd(true);
            }

            this.ignoringExtraWhitespaces = ignoringExtraWhitespaces;
        }
    }

    /**
     * Indicates if processing is namespace aware.
     * 
     * @param namespaceAware
     *            Indicates if processing is namespace aware.
     */
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    /**
     * Sets the map of namespaces.
     * 
     * @param namespaces
     *            The map of namespaces.
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }



    /**
     * Indicates the desire for validating this type of XML representations
     * against an XML schema if one is referenced within the contents.
     * 
     * @param validating
     *            The new validation flag to set.
     */
    public void setValidatingDtd(boolean validating) {
        this.validatingDtd = validating;
    }

    /**
     * Indicates the desire for processing <em>XInclude</em> if found in this
     * type of XML representations. By default the value of this is set to
     * false.
     * 
     * @param includeAware
     *            The new value of the xIncludeAware flag.
     */
    public void setXIncludeAware(boolean includeAware) {
        xIncludeAware = includeAware;
    }





}
