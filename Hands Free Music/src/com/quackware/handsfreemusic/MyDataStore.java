package com.quackware.handsfreemusic;

public class MyDataStore 
{
	//final CharSequence[][] songResults, boolean albumSearch,String command)
	
	private String mCommand;
	private CharSequence[][] mSongResults;
	private CharSequence[] mShuffleSongs;
	private boolean mAlbumSearch;
	private boolean mIsDialogShowing;
	private int mRequestCode;
	private boolean mLoadingSongList = false;
	
	private String mOneFilePath;
	//private HandsFreeMusic.ProgressThread mProgressThread; 
	
	public MyDataStore(CharSequence[][] songResults, CharSequence[] iShuffleSongs, boolean albumSearch, String command, boolean isDialogShowing, String iOneFilePath,
			int iRequestCode, boolean iLoadingSongList)
	{
		setmSongResults(songResults);
		mShuffleSongs = iShuffleSongs;
		setmAlbumSearch(albumSearch);
		setmCommand(command);
		setmIsDialogShowing(isDialogShowing);
		setmOneFilePath(iOneFilePath);
		setmRequestCode(iRequestCode);
		setmLoadingSongList(iLoadingSongList);
	}

	public void setmCommand(String mCommand) {
		this.mCommand = mCommand;
	}

	public String getmCommand() {
		return mCommand;
	}

	public void setmSongResults(CharSequence[][] mSongResults) {
		this.mSongResults = mSongResults;
	}

	public CharSequence[][] getmSongResults() {
		return mSongResults;
	}

	public void setmAlbumSearch(boolean mAlbumSearch) {
		this.mAlbumSearch = mAlbumSearch;
	}

	public boolean ismAlbumSearch() {
		return mAlbumSearch;
	}

	public void setmIsDialogShowing(boolean mIsDialogShowing) {
		this.mIsDialogShowing = mIsDialogShowing;
	}

	public boolean ismIsDialogShowing() {
		return mIsDialogShowing;
	}

	public void setmOneFilePath(String mOneFilePath) {
		this.mOneFilePath = mOneFilePath;
	}

	public String getmOneFilePath() {
		return mOneFilePath;
	}

	public void setmRequestCode(int mRequestCode) {
		this.mRequestCode = mRequestCode;
	}

	public int getmRequestCode() {
		return mRequestCode;
	}

	public void setmLoadingSongList(boolean mLoadingSongList) {
		this.mLoadingSongList = mLoadingSongList;
	}

	public boolean ismLoadingSongList() {
		return mLoadingSongList;
	}
}