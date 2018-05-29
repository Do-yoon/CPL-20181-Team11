package org.duckdns.gong.bot;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

// 하나의 ListView를 구성하는 아이템들을 선언
class AppListRowItem {
    private Drawable appIcon;
    private String appName;
    private String appPackageName;
    private CheckBox checkbox;

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public void setCheckbox(CheckBox checkbox) {
        this.checkbox = checkbox;
    }

    public CheckBox getCheckbox() {
        return checkbox;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public String getAppName() {
        return appName;
    }
}

class AppListAdapter extends BaseAdapter {
    private ArrayList<AppListRowItem> appListRowItems = new ArrayList<AppListRowItem>();
    private SharedPreferences appNotiEnabled;
    private Context context;

    public AppListAdapter(Context context) {
        this.context = context;
        // 체크박스 값을 저장할 SharedPreferences를 선언
        appNotiEnabled = context.getSharedPreferences("appNotificationEnabled", Activity.MODE_PRIVATE);
    }

    @Override
    public int getCount() {
        return appListRowItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_row_layout, parent, false);
            holder = new ViewHolder();

            holder.appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
            holder.appName = (TextView) convertView.findViewById(R.id.appName);
            holder.appPackageName = (TextView) convertView.findViewById(R.id.appPackageName);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkBox);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppListRowItem appListRowItem = appListRowItems.get(position);

        holder.appIcon.setImageDrawable(appListRowItem.getAppIcon());
        holder.appName.setText(appListRowItem.getAppName());
        holder.appPackageName.setText(appListRowItem.getAppPackageName());

        // SharedPreferences에 저장된 값들을 읽어들여 이전의 상태를 복구
        boolean isAppNotiEnabled = appNotiEnabled.getBoolean(holder.appName.getText().toString(), false);
        if (isAppNotiEnabled) {
            holder.checkbox.setChecked(true);
        } else {
            holder.checkbox.setChecked(false);
        }

        // 체크박스의 값이 변경될 때 마다 SharedPreference에 추가하거나 삭제
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final SharedPreferences.Editor editor = appNotiEnabled.edit();

                if (holder.checkbox.isChecked()) {
                    editor.putBoolean(holder.appName.getText().toString(), true).commit();
                } else {
                    editor.remove(holder.appName.getText().toString()).commit();
                }
            }
        });

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return appListRowItems.get(position);
    }

    // 아이템들을 추가
    public void addItem(Drawable appIcon, String appName, String appPackageName, CheckBox checkbox) {
        AppListRowItem item = new AppListRowItem();

        item.setAppIcon(appIcon);
        item.setAppName(appName);
        item.setAppPackageName(appPackageName);
        item.setCheckbox(checkbox);

        appListRowItems.add(item);
    }

    private static class ViewHolder {
        public ImageView appIcon;
        public TextView appName;
        public TextView appPackageName;
        public CheckBox checkbox;
    }
}

// ACTION_MAIN과 CATEGORY_LAUNCHER를 인텐트 필터로 가지는 어플리케이션을 찾아 어댑터에 추가시켜 준다
public class AppListFragment extends ListFragment {
    AppListAdapter alAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        alAdapter = new AppListAdapter(getActivity());
        setListAdapter(alAdapter);

        PackageManager pm = getActivity().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo appInfo : appInfos) {
            alAdapter.addItem(appInfo.activityInfo.applicationInfo.loadIcon(pm),
                    appInfo.activityInfo.loadLabel(pm).toString(),
                    appInfo.activityInfo.packageName,
                    new CheckBox(getActivity()));
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        // 액션바 제목 설정 및 백버튼 추가
        ab.setTitle("어플리케이션 알림 설정");
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 화면상의 백버튼이 터치될 경우 백버튼을 실행
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    // 리스트뷰가 클릭이 될 경우 클릭된 리스트뷰의 체크박스 상태를 반전
    public void onListItemClick(ListView l, View v, int position, long id) {
        CheckBox checkbox = (CheckBox) v.findViewById(R.id.checkBox);
        if (checkbox.isChecked())
            checkbox.setChecked(false);
        else
            checkbox.setChecked(true);
    }
}
