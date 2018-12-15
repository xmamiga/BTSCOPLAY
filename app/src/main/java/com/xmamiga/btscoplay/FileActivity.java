package com.xmamiga.btscoplay;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.xmamiga.myapplication.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileActivity extends Activity {
    public final static String EXTRA_FILENAME = "ti.android.ble.devicemonitor.FILENAME";
    private FileAdapter mAdapter;
    private ListView lvFile;
    private Button mConfirm, tvBack;
    private List<File> mFileList = new ArrayList<File>();
    ;
    TextView mEmptyView = null;
    private File mCurrentPathFile = null;
    private static final int REQ_SYSTEM_SETTINGS = 0;
    List<File> mbackwardfiles = null;
    private boolean misShowHiddenFiles = false;

    public FileActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        mEmptyView = (TextView) findViewById(R.id.empty);
        mbackwardfiles = new ArrayList<File>();
        mConfirm = findViewById(R.id.tv_confirm);
        mConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (mAdapter.getSelectedFile() != null
                        && mAdapter.getSelectedFile().isFile()) {
                    Log.e("xmamiga", "mSelectedFile: "
                            + mAdapter.getSelectedFile().getAbsolutePath());
                    intent.putExtra(EXTRA_FILENAME, mAdapter.getSelectedFile()
                            .getAbsolutePath());
                    Log.e("xmamiga", "mSelectedParentvFile: "
                            + mAdapter.getSelectedFile().getParentFile()
                            .getAbsolutePath());
                    setResult(Constants.RESULTCODE_fILE, intent);
                }
                finish();
            }
        });
        tvBack = findViewById(R.id.tv_back);
        tvBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        lvFile = (ListView) findViewById(R.id.lw_file);
        lvFile.setOnItemClickListener(mFileClickListener);
        lvFile.setEmptyView(mEmptyView);

        // Characteristics list
        mAdapter = new FileAdapter(this);
        lvFile.setAdapter(mAdapter);
        if (mFileList.size() < 0)
            mConfirm.setText(R.string.cancel);
        String mFileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "Download";
        initData(mFileDir);
    }

    @Override
    public void onDestroy() {
        mFileList = null;
        mAdapter = null;
        super.onDestroy();
    }

    private OnItemClickListener mFileClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            File mselectedFile = mAdapter.getItem(pos);
            if (mselectedFile != null && !mselectedFile.isFile()) {
                open(mselectedFile, true);
            } else if (mselectedFile.isFile()) {
                mAdapter.setSelectedPosition(pos);
            }
        }
    };

    private void initData(String path) {
        File file = new File(path);
        loadSettings();
        if (file != null && file.canRead()) {
            open(file, false);
        }
    }

    private void deleteAllItems() {
        mFileList.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void open(File f, boolean misAddToBackWardFiles) {
        if (f == null) {
            return;
        }
        if (!f.exists()) {
            return;
        }
        if (f.isFile()) {
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                String type = FileUtils.getMIMEType(f);
                intent.setDataAndType(Uri.fromFile(f), type);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        } else if (f.isDirectory()) {
            deleteAllItems();
            mCurrentPathFile = f;
            if (misAddToBackWardFiles) {
                mbackwardfiles.add(mCurrentPathFile.getParentFile());
            }
            File[] files = f.listFiles();
            if (files != null) {
                Arrays.sort(files, new FileComparator());
                for (File file : files) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(".wav")) {
                            mFileList.add(file);
                        }
                    }
                }
            } else {
                open(new File("/sdcard"), false);
                mCurrentPathFile = null;
            }
            mAdapter.setData(mFileList);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void upward() {
        if (mCurrentPathFile == null
                || (mCurrentPathFile.getAbsolutePath()).equals("/")) {
            finish();
        } else {
            File f = mCurrentPathFile;
            if (!mCurrentPathFile.getAbsolutePath().equals("/")) {
                open(f.getParentFile(), true);
            }
        }
    }

    private boolean loadSettings() {
        boolean ret = true;
        String key_showhidden = getResources().getString(
                R.string.preference_showhidden_key);
        boolean default_value_showhidden = Boolean.parseBoolean(getResources()
                .getString(R.string.preference_showhidden_default_value));
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        misShowHiddenFiles = settings.getBoolean(key_showhidden,
                default_value_showhidden);
        return ret;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SYSTEM_SETTINGS) {
            loadSettings();
            open(mCurrentPathFile, false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                upward();
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);

    }

}
