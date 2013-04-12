package challenge.netlinks.insat.megaarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends Activity{
	private final int SPLASH_DISPLAY_LENGTH = 3000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                Splash.this.finish();
                Intent mainIntent = new Intent(Splash.this, Main.class);
                Splash.this.startActivity(mainIntent);
            }
        }, SPLASH_DISPLAY_LENGTH);
	}
}
