package com.anglab.smstelegram;

import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListViewer extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	static class ListAdapterWithButton<T> extends BaseAdapter {
		private final LayoutInflater mInflater;
		private final List<HashMap<String, String>> array;

		public ListAdapterWithButton(final Context context, final List<HashMap<String, String>> array) {
			this.mInflater = LayoutInflater.from(context);
			this.array = array;
		}

		@Override
		public int getCount() { return array.size(); }

		@Override
		public String getItem(int position) { return (String)array.get(position).get("NAME"); }

		@Override
		public long getItemId(int position) { return position; }

		class ViewHolder {
			TextView label;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if ( convertView == null ) {
				convertView = mInflater.inflate(R.layout.list_one_row, null);
				holder = new ViewHolder();
				holder.label = (TextView) convertView.findViewById(R.id.txt_title);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			
			String line ="\n--=============--\n"; 

			String $msg = getList(position, "DPL")+"//"+ getList(position, "SND_NUM")+getList(position, "SND_NM")+line+getList(position, "MSG_TXT")+line;
		    String today = aUtil.convDate(getList(position, "RCV_DH"));
		   // System.out.println("today==="+today);

		    $msg += getList(position, "RCV_NUM")+line+ today;
		    $msg += line+aUtil.convDate(getList(position, "TRN_DH"))+line+aUtil.convDate(getList(position, "TL_COMP_DH"));
			//String vName = getList(position, "body");
		    

			String vName = $msg;
			
			//String vName = getList(position, "NAME");
			holder.label.setText(vName); // 리스트 제목

			// 자료가 없다 = 검색된 결과가 없습니다
			if ( 1==1 // vMainActivity.getResources().getString(R.string.str_noSearchData).equals(vName)
			  && array.size() == 1 ) {
				holder.label.setTextColor(0xFF333333);
				return convertView;
			}

			final String vMode = "1";// vMainActivity.fn_getMode();
			/********** 사이트 썸네일 **********/
			String vSite = getList(position, "SITE");
			int vDrawable = -1;
			/*
			if ( "naver".equals(vSite) ) vDrawable = R.drawable.ic_naver;
			else if ( "nate".equals(vSite) ) vDrawable = R.drawable.ic_nate;
			else if ( "daum".equals(vSite) ) vDrawable = R.drawable.ic_daum;
			else if ( "naver_b".equals(vSite) ) vDrawable = R.drawable.ic_naver_b;
			else if ( "daum_l".equals(vSite)  ) vDrawable = R.drawable.ic_daum_l;
			else if ( "kakao".equals(vSite)  ) vDrawable = R.drawable.ic_kakao;
			else if ( "lezhin".equals(vSite)  ) vDrawable = R.drawable.ic_lezhin;
			else if ( "olleh".equals(vSite)  ) vDrawable = R.drawable.ic_olleh;
			else if ( "tstore".equals(vSite)  ) vDrawable = R.drawable.ic_tstore;
			else if ( "ttale".equals(vSite)  ) vDrawable = R.drawable.ic_ttale;
			else if ( "foxtoon".equals(vSite)  ) vDrawable = R.drawable.ic_foxtoon;
			else if ( "foxtoon_d".equals(vSite)  ) vDrawable = R.drawable.ic_foxtoon;
			else if ( "daum_s".equals(vSite)  ) vDrawable = R.drawable.ic_daum; // TODO 다음 스포츠 추가 ks20151210
*/
			/********************/

			

			/********** 유료표시여부 ks20151218 **********/
			if ( "Y".equals(getList(position, "SELL_YN")) ) {

				holder.label.setText("(유료) "+vName); // 리스트 제목
				//holder.btn_list_01.setVisibility(View.VISIBLE);
			} else {
				//holder.web_thumb.setVisibility(View.INVISIBLE);
			}
			/********************/
			
			return convertView;
		}

		public String getList(int pPosition, String pTag) {
			if ( getCount() <= pPosition && 0 > pPosition ) return "";
			if ( !array.get(pPosition).containsKey(pTag) ) return "";
			String vReturn = (String) array.get(pPosition).get(pTag);
			if ( vReturn == null || vReturn.trim().length() == 0 ) vReturn = "";
			return vReturn;
		}
	}
}