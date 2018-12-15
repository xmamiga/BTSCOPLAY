package com.xmamiga.btscoplay

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log

/**
 * Created by xmamiga on 2018/12/14
 */

class Player(context: Context) {

    public interface RecoderListener {
        fun onData(data: ByteArray)
    }

    private lateinit var mAudioManager: AudioManager
    private var mRecordingThread: RecordThread? = null
    var mListener: RecoderListener? = null


    init {
        mAudioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
    }

    fun startRecord(listener: RecoderListener) {
        mListener = listener

        if (mAudioManager.isBluetoothScoAvailableOffCall) {

            if (mAudioManager.isBluetoothScoOn) {
                mAudioManager.stopBluetoothSco()
                Log.e("xmamiga", "1mAudioManager.stopBluetoothSco()")
            }
            Log.e("xmamiga", "1startBluetoothSco")
            mAudioManager.startBluetoothSco()
            var timeout = 100
            while (!mAudioManager.isBluetoothScoOn && timeout-- > 0) {
                Thread.sleep(10)
                if (timeout == 50) {
                    Log.e("xmamiga", "2startBluetoothSco")
                    mAudioManager.startBluetoothSco()
                }
                Log.e("xmamiga", "change BluetoothScoOn" + mAudioManager.isBluetoothScoOn + ":" + timeout)
            }
            stopRecord();
            mRecordingThread = RecordThread()
            mRecordingThread!!.start()
        }
    }

    fun stopRecord() {
        if (mRecordingThread != null) {
            mRecordingThread!!.pause()
            mRecordingThread!!.interrupt()
            if (mAudioManager.isBluetoothScoOn) {
                mAudioManager.stopBluetoothSco()
                Log.e("xmamiga", "1mAudioManager.stopBluetoothSco()")
            }
        }
    }

    fun getState(): Boolean {
        if (mRecordingThread != null) {
            return mRecordingThread!!.isRun
        }
        return false
    }

    private var SAMPLE_RATE_HZ = 8000

    fun getSize(): Int {
        if (mRecordingThread != null) {
            return mRecordingThread!!.bufferSize
        }
        return 0;
    }

    fun setRateHZ(samplE_RATE_HZ: Int) {
        this.SAMPLE_RATE_HZ = samplE_RATE_HZ;
    }

    internal inner class RecordThread : Thread() {
        private val audioRecord: AudioRecord
        val bufferSize: Int
        var isRun: Boolean = false

        private var mStartTime = 0L

        init {

            var audiosource = MediaRecorder.AudioSource.VOICE_RECOGNITION
            if (Build.VERSION.SDK_INT > 19) {
                audiosource = MediaRecorder.AudioSource.VOICE_COMMUNICATION
            }
            this.bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT) * 2
            this.audioRecord = AudioRecord(audiosource,
                    SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    this.bufferSize)

        }

        override fun run() {
            super.run()
            this.isRun = true
            try {
                if (audioRecord.state == 1) {
                    mStartTime = System.currentTimeMillis()

                    while (this.isRun) {
                        val buffer = ByteArray(bufferSize)
                        val readBytes = audioRecord.read(buffer, 0, bufferSize)
                        if (readBytes > 0) {
                            if (mListener != null) {
                                mListener!!.onData(buffer)
                            }
                        }
                    }

                    try {
                        this.audioRecord.stop()
                        this.audioRecord.release()
                    } catch (audioException: Exception) {

                    }

                    Log.e("xmamiga", "endVoiceRequest() --> ")
                }
            } catch (e2: Exception) {
                Log.e("xmamiga", "error: " + e2.message)
                try {
                    this.audioRecord.stop()
                    this.audioRecord.release()
                } catch (audioException: Exception) {
                }

                isRun = false
            }
        }

        fun pause() {
            this.isRun = false
            try {
                this.audioRecord.stop()
                this.audioRecord.release()
            } catch (e: Exception) {
            }
        }

        @Synchronized
        override fun start() {
            if (!isRun) {
                super.start()
            }
        }
    }
}
