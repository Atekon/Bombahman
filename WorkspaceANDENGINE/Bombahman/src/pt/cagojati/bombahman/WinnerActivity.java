package pt.cagojati.bombahman;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class WinnerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_winner);
		
		Bundle bundle = getIntent().getExtras();
		if(bundle.getBoolean("isWinner")){
			TextView txtView = (TextView) findViewById(R.id.StatusText);
			txtView.setText(R.string.victory);
		}
		
		int x = bundle.getInt("winnerId");
		String str = this.getString(R.string.playerWon);
		str = str.replace("X", ""+(x+1));
		TextView txtView = (TextView) findViewById(R.id.WinnerText);
		txtView.setText(str);
	}

}
