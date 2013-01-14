package pt.cagojati.bombahman;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.widget.RelativeLayout;
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
		int playerWonImage = 0;
		switch (x) {
		case 0:
			playerWonImage = R.drawable.player1won;
			break;
		case 1:
			playerWonImage = R.drawable.player2won;
			break;
		case 2:
			playerWonImage = R.drawable.player3won;
			break;
		case 3:
			playerWonImage = R.drawable.player4won;
			break;
		}
		RelativeLayout winnerLayout = (RelativeLayout) this.findViewById(R.id.WinnerLayoutRelative);
		winnerLayout.setBackgroundResource(playerWonImage);
	}

}
