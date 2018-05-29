package org.duckdns.gong.bot;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        Preference notiPref=(Preference)findPreference("pref_key_noti_app");

        // 액션바의 제목을 변경
        ab.setTitle("설정");
        ab.setDisplayHomeAsUpEnabled(false);

        // 설정창에서 어플리케이션이 클릭될 경우 어플 리스트를 출력해주는 AppListFragment로 교체
        notiPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content,new AppListFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            }
        });
    }
}