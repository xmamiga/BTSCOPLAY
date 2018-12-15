package com.xmamiga.btscoplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xmamiga.myapplication.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends BaseAdapter {

    private List<File> mFiles = new ArrayList<File>();
    private Context mContext;
    private File mSelectedFile;
    private int mSelectedPos;

    public FileAdapter(Context context) {
        mContext = context;
        mSelectedPos = -1;
    }

    public void setData(List<File> files) {
        mFiles.clear();
        mFiles.addAll(files);
    }

    public void setSelectedPosition(int i) {
        mSelectedFile = mFiles.get(i);
        mSelectedPos = i;
        notifyDataSetChanged();
    }

    public File getSelectedFile() {
        return mSelectedFile;
    }

    @Override
    public int getCount() {
        return mFiles.size();
    }

    @Override
    public File getItem(int i) {
        if ((i >= 0) && (i < this.getCount()))
            return mFiles.get(i);
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (null == view) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.item_listview_file, null);
            holder = new ViewHolder();
            holder.mfileName = (TextView) view.findViewById(R.id.name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        File f = this.getItem(i);
        holder.mfileName.setText(f.getName());
        if (i == mSelectedPos) {
            holder.mfileName.setTextAppearance(mContext,
                    R.style.nameStyleSelected);
        } else {
            holder.mfileName.setTextAppearance(mContext, R.style.nameStyle);
        }
        return view;
    }

    private class ViewHolder {
        private TextView mfileName;
    }

}
