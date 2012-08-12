package com.quackware.handsfreemusic;


import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class HelpActivity  extends Activity implements OnClickListener
{
	private static final String FAQ_TEXT = "Frequently Asked Questions: <br /> <br />"
		+ "Q: Why does voice recognition take so long? / Why am I receiving voice errors? <br /> <br />"
		+ "A: This is not the application's fault, rather you are unable to connect to Google's server in order "
		+ "to handle voice recognition. Try again later in a location with a better signal. Please do not report this "
		+ "problem as an application problem, as I have no control over it. <br /> <br />"
		+ "Q: Where can I report bugs / suggest features / contact you? <br /> <br />"
		+ "A: You can contact me by email at QuackWare@gmail.com or visit http://www.Quack-Ware.com/android for updates <br /> <br />"
		+ "Q: I recently added some songs and they are not being found, help! <br /> <br />"
		+ "A: Go to Preferences and tap \" Rebuild Song Index \" <br /> <br />"
		+ "Q: Why isn't the song I want on YouTube? <br /> <br />"
		+ "A: Many videos on YouTube are prevented from playing on mobile devices. Unfortunately there is nothing I can do about this <br /> <br />"
		+ "Q: What are binds? <br /> <br />"
		+ "A: Binds are ways in which you can replace saying complicated or difficult to recognize words or phrases with simple words or phrases.<br /> <br />"
		+ "Q: What versions of this application are there in the market? <br /> <br />"
		+ "A: There is an ad supported version that is free, and a paid version without ads that is $0.99 <br /> <br /> <br />"
		+ "This application is still in development, so please be patient with any bugs or crashes you encounter."
		+ "I will try to fix them as soon as possible, hopefully with your help. If you enjoy this application please "
		+ "rate it high! :)";
	
	private static final String HOWTO_TEXT = "How To: <br /> <br />"
		+ "1) Click on of the large mic button on the main screen<br /> <br />"
		+ "2) Wait until the microphone appears and say your command clearly and loudly into the mic. For a list of commands check the command help menu. <br /> <br />"
		+ "3) Wait patiently until your results are retrieved. If there is an error then try to move into an"
		+ "area with better signal. If the song is not found then try another. <br /> <br />"
		+ "4) You can also set custom binds to make your search experience easier. <br /> <br />"
		+ "5) If you enjoy this app then make sure to buy the ad free version! <br /> <br />"
		+ "PS: You can specify what you want to search for by pressing the mic button and holding for a few seconds.";
	
	private static final String EXTRAS_TEXT = "If you enjoy this application please purchase the ad free version! <br /> <br />"
		+ "It is exactly the same except there are no ads to bother you :) <br /> <br />"
		+ "Search \"Hands Free Music Donate\" in the app store to find it";
	
	private static final String COMMANDS_TEXT = "List of spoken commands: <br /> <br />"
		+ "song [songname] : Returns a list of songs based on the song name you said. <br /> <br />"
		+ "artist [artistname] : Returns a list of songs based on the artist you said. <br /> <br />"
		+ "album [albumname] : Returns a list of songs based on the album you said. <br /> <br />"
		+ "playlist [playlistname] : Returns a list of songs based on the playlist you said. <br /> <br />"
		+ "youtube [songname] : Returns a list of songs from YouTube based on the song name you said <br /> <br />"
		+ "If you do not specify a command, the application will return a list of songs based on song name. <br /> <br />"
		+ "To search for song, artist, album, or playlist without saying a command. Push the large microphone for a few seconds until a menu appears, then select your option.";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.helpmain);
        
        
        findViewById(R.id.howTo).setOnClickListener(this); 
        findViewById(R.id.faq).setOnClickListener(this);
        findViewById(R.id.extras).setOnClickListener(this);
        findViewById(R.id.commands).setOnClickListener(this);
        
    }

	public void onClick(View v) {
		switch(v.getId()) 
		{
		case R.id.howTo:
			setContentView(R.layout.help);
			((TextView)findViewById(R.id.helpText)).setText(Html.fromHtml(HOWTO_TEXT));
			break;
		case R.id.faq:
			setContentView(R.layout.help);
			((TextView)findViewById(R.id.helpText)).setText(Html.fromHtml(FAQ_TEXT));
			break;
		case R.id.extras:
			setContentView(R.layout.help);
			((TextView)findViewById(R.id.helpText)).setText(Html.fromHtml(EXTRAS_TEXT));
			break;
		case R.id.commands:
			setContentView(R.layout.help);
			((TextView)findViewById(R.id.helpText)).setText(Html.fromHtml(COMMANDS_TEXT));
		default:
			break;
		
		}
		
	}
}
