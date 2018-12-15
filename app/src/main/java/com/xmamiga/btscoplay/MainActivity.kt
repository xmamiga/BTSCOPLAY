package com.xmamiga.btscoplay

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.xmamiga.myapplication.R
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileInputStream


class MainActivity : AppCompatActivity() {

    private lateinit var mAudioManager: AudioManager
    private var SAMPLE_RATE_HZ: Int = 8000
    private var playerBufSize: Int = 0
    private var player: AudioTrack? = null
    private var mPlayer: Player? = null
    private var mFileName: String = ""
    private var isPlaying: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SAMPLE_RATE_HZ = 16000
        btn_8000.setTextColor(Color.BLACK)
        btn_16000.setTextColor(Color.RED)
        initPermission()
        tv_chose_path.setOnClickListener {
            pausePlyaer()
            val intent = Intent(this@MainActivity, FileActivity::class.java)
            startActivityForResult(intent, Constants.REQUESTCODE_FILE)
        }
        btn_8000.setOnClickListener {
            SAMPLE_RATE_HZ = 8000;
            pausePlyaer()
            btn_8000.setTextColor(Color.RED)
            btn_16000.setTextColor(Color.BLACK)
        }
        btn_16000.setOnClickListener {
            SAMPLE_RATE_HZ = 16000;
            pausePlyaer()
            btn_8000.setTextColor(Color.BLACK)
            btn_16000.setTextColor(Color.RED)
        }
        tv_start.setOnClickListener {
            if (TextUtils.isEmpty(mFileName)) {
                Toast.makeText(this@MainActivity, getString(R.string.select_file), Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }
            Thread(Runnable {
                pausePlyaer()
                initPlayer();
                mPlayer!!.startRecord(object : Player.RecoderListener {
                    override fun onData(data: ByteArray) {
                    }
                })
                val fis = FileInputStream(mFileName)
                val buffer = ByteArray(mPlayer!!.getSize())
                while (fis.available() > 0 && isPlaying) {
                    val readCount = fis.read(buffer)
                    if (readCount == -1) {
                        break
                    }
                    player!!.write(buffer, 0, buffer.size)
                }
            }).start()
        }
        tv_pause.setOnClickListener {
            pausePlyaer()
        }
        tv_version.setText(getString(R.string.current_version) + getVersion())
    }

    private fun pausePlyaer() {
        isPlaying = false;
        if (mPlayer != null) {
            mPlayer!!.stopRecord()
        }
        if (player != null) {
            player!!.stop()
            player!!.release()
            player = null
        }
    }

    private fun initPlayer() {
        mAudioManager = getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
        playerBufSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT)
        player = AudioTrack(
                AudioManager.STREAM_VOICE_CALL,
                SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                playerBufSize,
                AudioTrack.MODE_STREAM)
        mPlayer = Player(this)
        mPlayer!!.setRateHZ(SAMPLE_RATE_HZ)
        isPlaying = true
        player!!.play()
    }

    private fun initPermission() {
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)
        val toApplyList = ArrayList<String>()
        for (perm in permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm)
                //进入到这里代表没有权限.
            }
        }

        if (!toApplyList.isEmpty()) {
            val tmpList = arrayOfNulls<String>(toApplyList.size)
            ActivityCompat.requestPermissions(this, toApplyList.toTypedArray(), 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUESTCODE_FILE && resultCode == Constants.RESULTCODE_fILE) {
            val filename = data!!.getStringExtra(FileActivity.EXTRA_FILENAME)
            Log.e("xmamiga", "filename: " + filename);
            mFileName = filename;
            tv_file_name.setText(mFileName)
        }
    }

    fun getVersion(): String {
        try {
            val manager = this.packageManager
            val info = manager.getPackageInfo(this.packageName, 0)
            return info.versionName
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}
