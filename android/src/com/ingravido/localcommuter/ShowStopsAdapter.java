package com.ingravido.localcommuter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class ShowStopsAdapter extends BaseAdapter {
	Activity activity;
	ArrayList<Stop> items;
	private Context mContext;

	private LayoutInflater mLayoutInflater;
	
	public ShowStopsAdapter(Context mContext, ArrayList<Stop> items){
		this.mContext = mContext;
		this.items = items;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

//		   RelativeLayout itemView;
//	          if (convertView == null) {                                        
//	             itemView = (RelativeLayout) mLayoutInflater.inflate(
//	                      R.layout.listview_item_row, parent, false);
//	 
//	          } else {
//	             itemView = (RelativeLayout) convertView;
//	          }
	          
	          if (convertView == null) {
	              LayoutInflater inflater = (LayoutInflater) this.mContext
	                      .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	              convertView = (View) inflater.inflate(
	                      R.layout.listview_item_row, null);
	          }
	          TextView tvtitle = (TextView) convertView.findViewById(R.id.listTitle);
	          TextView tvdesc = (TextView) convertView.findViewById(R.id.listDescription);
	          
	          tvtitle.setText(items.get(position).getName());
	          
//	          String str = Long.toString(items.get(position).getLat()) +Long.toString(items.get(position).getLng());
	          String str = Double.toString(items.get(position).getLat()) + " " + Double.toString(items.get(position).getLng());
	          //tvdesc.setText((String) Long.toString(items.get(position).getLat()));
	          tvdesc.setText(str);
	
	          
		return convertView;
	}
}
