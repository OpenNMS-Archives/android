package com.opennms.android.outages;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.opennms.android.R;

public class OutageAdapter extends ArrayAdapter<Outage> {
	private List<Outage> m_items;
	private Context m_context;

	public OutageAdapter(final Context context, final int textViewResourceId, final List<Outage> items) {
		super(context, textViewResourceId, items);
		m_context = context;
		m_items = items;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.severity_item, null);
		}
		final Outage o = m_items.get(position);
		if (o != null) {
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
			if (tt != null) {
				String host = o.getHost();
				if (host == null) {
					host = o.getIpAddress();
				}
				tt.setText(host);
			}
			if (bt != null) {
				final String description = o.getDescription().replaceAll("<p>", "").replaceAll("</p>", "<br />").replace("<br />$", "");
				bt.setText(Html.fromHtml(description), BufferType.SPANNABLE);
			}
		}
		return v;
	}
}
