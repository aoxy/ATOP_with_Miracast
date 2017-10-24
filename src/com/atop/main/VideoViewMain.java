package com.atop.main;

import java.io.File;

import com.atop.keyboard.KeyboardMouseActivity;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class VideoViewMain extends SurfaceView implements Callback {
	private final String TAG = "Class::VideoView";
	private static int[] FILE_ARR;
	private static int DelayTime = 5000;
	private SurfaceHolder mSurfaceHolder;
	private SurfaceHolder mholder;
	public static MediaPlayer mPlayer;
	String path = "/sdcard/ATOP/Stream/temp";
	String fileBack = "";
	private static int CUR_FILE_NUM = -1;
	private File CUR_file;
	boolean ispause = false;
	boolean isSeek = true;
	boolean isPlay = true;
	boolean user_seekto = false;
	int user_seek_time = 0;
	int seekcntNext = 0;
	int seekcntBack = 0;
	int DelayMode = 0;
	SeekbarAsync seekbarA;
	private int AllfileSize = 0;
	private int cutfileSize = 0;
	private int Alltime = 0;
	private int cutAlltime = 0;
	int touchtime = 0;
	private boolean isfinish = false;
	private boolean isStop = false;

	public VideoViewMain(Context context) {
		super(context);
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		init();
	}

	public void init() {
		FILE_ARR = new int[21];
	}

	public boolean hasfile(int cur) {
		if (FILE_ARR[cur] == 1) {
			return true;
		} else {
			return false;
		}
	}

	public void video_Seekto(int filenum, int time) {
		Log.e(TAG, "filenum : " + filenum);
		user_seekto = true;
		CUR_FILE_NUM = filenum - 1;
		this.user_seek_time = time;
		ispause = true;
		videoPlay(1);
	}

	public void setAllfileSize(int size, int time) {
		this.Alltime = time;
		this.AllfileSize = size;
		cutfileSize = AllfileSize / 20;
		cutAlltime = Alltime / 20;
	}

	public void saveFile(int check) {
		FILE_ARR[check] = 1;
	}

	private String getpath(boolean up) {
		CUR_FILE_NUM++;
		String rpath = path + CUR_FILE_NUM + fileBack;
		if (up == false) {
			CUR_FILE_NUM--;
		}
		return rpath;
	}

	private String backpath(boolean down) {
		CUR_FILE_NUM--;
		String rpath = path + CUR_FILE_NUM + fileBack;
		if (down == false)
			CUR_FILE_NUM++;
		return rpath;
	}

	public void NextVideoPre() {
		DelayMode = 1;
		seekbarA.isfind = true;
		if (CUR_FILE_NUM != -1){
			KeyboardMouseActivity.sendMediaseek(CUR_FILE_NUM + 1);
			isPlay = true;
			}
		KeyboardMouseActivity.setVideoState_ready();
	}

	public void BackVideoPre() {
		DelayMode = 2;
		seekbarA.isfind = true;
		if (CUR_FILE_NUM != -1){
			KeyboardMouseActivity.sendMediaseek(CUR_FILE_NUM - 1);
			}
		KeyboardMouseActivity.setVideoState_ready();
	}

	public void videoPlay(int mode) {
		if (DelayMode != 0) {
			seekbarA.isfind = false;
			DelayMode = 0;
			KeyboardMouseActivity.setVideoState_start();
		}

		if (mode == 1) {

			CUR_file = new File(getpath(false)); // ������ �����ϸ� �÷���... �׷��� ������ ��¿?
			try {
				if (FILE_ARR[CUR_FILE_NUM + 1] == 1) {

					mPlayer.stop();
					mPlayer.reset();

					mPlayer.setDataSource(getpath(true));
					mPlayer.prepare();

					mPlayer.start();
					if (isStop) {
						mPlayer.pause();
						ispause = true;
					} else
						ispause = false;

					if (user_seekto) {
						Thread.sleep(500);
						mPlayer.seekTo(user_seek_time);
						user_seekto = false;
						user_seek_time = 0;
					}
				} else { // ��ø� ��ٷ��ּ��䤻�� �ٽ� Ʋ�� �������� cur file �� ����������
					seekbarA.isfind = true;
					isPlay = false;
					NextVideoPre();
				}
			} catch (Exception e) {// ��ø� ��ٷ��ּ��䤻��
				isPlay = false;
				Log.e(TAG, e + " : ���� �ֱ�");
			}
		} else {
			CUR_file = new File(backpath(false)); // ������ �����ϸ� �÷���... �׷��� ������ ��¿?
			try {
				if (FILE_ARR[CUR_FILE_NUM - 1] == 1) {
					mPlayer.stop();
					mPlayer.reset();

					mPlayer.setDataSource(backpath(true));
					mPlayer.prepare();
					mPlayer.start();
					ispause = false;
					int last = mPlayer.getDuration();
					mPlayer.seekTo(last - 5000);

				} else { // ��ø� ��ٷ��ּ��䤻��
					seekbarA.isfind = true;
					isPlay = false;
					BackVideoPre();
				}
			} catch (Exception e) {// ��ø� ��ٷ��ּ��䤻��
				isPlay = false;
			}
		}
	}

	public void startVideo(String fileback) {
		if (CUR_FILE_NUM == -1) { // �̰� ó����
			this.fileBack = fileback;
			seekbarA = new SeekbarAsync();
			seekbarA.execute();
			videoPlay(1);
		}
		if (ispause) {// �Ͻ����� �߿��� ������ ��������
			mPlayer.start();
			ispause = false;
		}
	}

	public void stopVideo() {
		mPlayer.stop();
		CUR_FILE_NUM = -1;
		videoPlay(1);
		isStop = true;
	}

	public void pauseVideo() {
		mPlayer.pause();
		ispause = true;
	}

	public void preVideo() {
		int t = mPlayer.getCurrentPosition();

		if (t + DelayTime + 1000 >= CUR_file.length()) {
			mPlayer.pause();
			isPlay = false;
			videoPlay(1); // ���� ģ���� Ʋ���ֱ�
		} else {
			if (seekcntNext < t || seekcntNext == 0) {
				mPlayer.seekTo(t + DelayTime);
				seekcntNext = t + DelayTime;
				seekcntBack = 0;
			} else
				seekcntNext = 0;
		}

	}

	public void backVideo() {
		int t = mPlayer.getCurrentPosition();
		if (t - DelayTime <= 0) {
			if (CUR_FILE_NUM == 0) {
				mPlayer.seekTo(0);
			} else {// ���� ģ���� Ʋ���ֱ�
				mPlayer.pause();
				isPlay = false;
				videoPlay(2);
			}
		} else {
			if (seekcntBack > t || seekcntBack == 0) {
				mPlayer.seekTo(t - DelayTime);
				seekcntBack = t;
				seekcntNext = 0;
			} else
				seekcntBack = 0;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mPlayer == null)
			mPlayer = new MediaPlayer();
		else
			mPlayer.reset();
		this.mholder = holder;

		mPlayer.setOnCompletionListener(mCom);
		mPlayer.setOnSeekCompleteListener(mSeek);
		mPlayer.setDisplay(mholder);

	}

	MediaPlayer.OnSeekCompleteListener mSeek = new MediaPlayer.OnSeekCompleteListener() {

		@Override
		public void onSeekComplete(MediaPlayer mp) {
			isSeek = true;
		}
	};

	MediaPlayer.OnCompletionListener mCom = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer mp) {
			Log.e(TAG, "mcom");
			if (isPlay && !isfinish) {
				Log.e(TAG, "play");
				videoPlay(1);
			} else {
				isPlay = true;
				Log.e(TAG, "�ѹ��� ����");
			}
		}
	};

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		CUR_FILE_NUM = -1;
		isfinish = true;
		if (seekbarA != null) {
			seekbarA.isasync = false;
			seekbarA.cancel(true);
			seekbarA = null;
		}
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	class SeekbarAsync extends AsyncTask<Void, Void, Void> {
		boolean isasync = true;
		boolean isfind = true;
		int checkfile = 0;
		int filelast = 0;
		int cur = 0;

		@Override
		protected void onPreExecute() { // �۾�������
			isasync = true;
			isfind = false;
		}

		@Override
		protected Void doInBackground(Void... params) { // ���ο��� �ϴ��۾�
			while (isasync && !isCancelled()) {
				if (isfind == true) {
					if (DelayMode == 1)
						checkfile = CUR_FILE_NUM + 1; // ������ �� ���ö� ���� ��ٸ���� ���̰�
					else if (DelayMode == 2)
						checkfile = CUR_FILE_NUM - 1; // ������ �� ���ö� ���� ��ٸ����
														// ���̴�.

					Log.e(TAG, "checkfile�� : " + checkfile);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (FILE_ARR[checkfile] == 1) {
						videoPlay(DelayMode);
						Log.e(TAG, "delaymode ��");
						isfind = false;
					}
				} else { // ã������ �ƴϸ� ���ư�
					if (ispause == false) { // �Ͻ������� �ƴϸ� ���ư�
						filelast = mPlayer.getDuration();
						cur = mPlayer.getCurrentPosition();

						long size = (long) ((cutfileSize * CUR_FILE_NUM));
						if (size < 0)
							size = 0;
						long tsize = (cutfileSize * cur) / filelast;
						KeyboardMouseActivity.media_seekbar
								.setProgress((int) (size + tsize));

						long time = (cutAlltime * CUR_FILE_NUM);
						if (time < 0)
							time = 0;

						KeyboardMouseActivity
								.setcurTime((int) (time + cur / 1000));

					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) { // �۾�������
			isasync = false;
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() { // ����Ҷ�
			isasync = false;
			super.onCancelled();
		}

		@Override
		protected void onProgressUpdate(Void... values) { // UI ������Ʈ
			super.onProgressUpdate(values);
		}

	}

}