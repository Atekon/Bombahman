package pt.cagojati.bombahman;

import org.andengine.opengl.vbo.LowMemoryVertexBufferObject;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class LobbyActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);
		
		Button btn_setTime = (Button) this.findViewById(R.id.SetTime_Btn);
		btn_setTime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//create horizontal linearLayout with 2 number pickers
				LinearLayout timeLayout = new LinearLayout(LobbyActivity.this);
				timeLayout.setOrientation(LinearLayout.HORIZONTAL);
				timeLayout.setGravity(Gravity.CENTER);
				NumberPicker time_minutes = new NumberPicker(LobbyActivity.this);
				time_minutes.setRange(1, 3);
				//disable keyboard in numberpicker
				time_minutes.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
				time_minutes.setLayoutParams(new NumberPicker.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.5f));
				NumberPicker time_seconds = new NumberPicker(LobbyActivity.this);
				time_seconds.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
				time_seconds.setLayoutParams(new NumberPicker.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.5f));
				time_seconds.setRange(0, 59);
				timeLayout.addView(time_minutes);
				timeLayout.addView(time_seconds);
				
				//create and show layout
				new AlertDialog.Builder(LobbyActivity.this)
			    .setTitle("Set Game Time")
			    .setView(timeLayout)
			    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            
			        }
			    }).show();
								
			}
		});
		
		//get screen size
		Display display = getWindowManager().getDefaultDisplay(); 
		int screenWidth = display.getWidth();  // deprecated
		int screenHeight = display.getHeight();
		
		//set height of map layout (image + buttons + map name)
		LinearLayout mapSelection = (LinearLayout) this.findViewById(R.id.Map_selection);
		LayoutParams params = mapSelection.getLayoutParams();
		params.height =(int) (screenHeight * 0.4); 
		mapSelection.setLayoutParams(params);
		
		//set height of options layout
		LinearLayout options = (LinearLayout) this.findViewById(R.id.OptionsVertical);
		params = options.getLayoutParams();
		params.height =(int) (screenHeight * 0.4);
		options.setLayoutParams(params);
		
//		Button ready_btn = (Button) this.findViewById(R.id.ReadyBtn);
//		ready_btn.setHeight((int)(screenHeight*0.1));
//		ready_btn.setLayoutParams(params);
		
		//Set height of the map's image
		ImageView mapImage = (ImageView) this.findViewById(R.id.MapImg);
		params = mapImage.getLayoutParams();
		params.height = (int) (screenHeight*0.33);
		mapImage.setLayoutParams(params);
	}

}
