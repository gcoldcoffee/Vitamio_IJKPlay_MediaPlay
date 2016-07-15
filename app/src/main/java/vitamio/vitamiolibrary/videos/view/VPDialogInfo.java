package vitamio.vitamiolibrary.videos.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import vitamio.vitamiolibrary.R;
import vitamio.vitamiolibrary.videos.mediaimpl.VPImpl;
import vitamio.vitamiolibrary.videos.utils.Alog;

/**
 * Dialog工具类,方便获取项目中所用到的各式Dialog,并包含显示与关闭Dialog常用方法
 */
@SuppressLint("InlinedApi")
public class VPDialogInfo extends Dialog {

	public Context context;

	public VPImpl.DialogImpl dialogImpl;

	public TextView titleinfo_textview, textview_no, textview_yes;

	public LinearLayout linear_no,linear_yes;

	public boolean isShotScreen=false;

	public VPDialogInfo(Context context, VPImpl.DialogImpl dialogImpl, boolean flag) {
		super(context,  R.style.weixin_dialog);
		this.context = context;
		this.dialogImpl = dialogImpl;
		this.isShotScreen=flag;
		initView();
	}

	public VPDialogInfo(Context context, int theme) {
		super(context, 0);
	}


	public void initView() {
		View dialogView = View.inflate(context, R.layout.vp_dialog,null);
		titleinfo_textview=(TextView)dialogView.findViewById(R.id.titleinfo_textview);
		textview_no=(TextView)dialogView.findViewById(R.id.textview_no);
		textview_yes=(TextView)dialogView.findViewById(R.id.textview_yes);
		linear_no=(LinearLayout)dialogView.findViewById(R.id.linear_no);
		linear_yes=(LinearLayout)dialogView.findViewById(R.id.linear_yes);

		setContentView(dialogView);

		setOnClickListener();

		setTextData();

	}

	private void setTextData(){
		if(isShotScreen){
			titleinfo_textview.setText(context.getResources().getString(R.string.shot_screen_success));
			textview_no.setText(context.getResources().getString(R.string.channel_publish));
			textview_yes.setText(context.getResources().getString(R.string.shotscreen_publish));
		}else{
			titleinfo_textview.setText(context.getResources().getString(R.string.not_find_wifi));
			textview_no.setText(context.getResources().getString(R.string.guarantee_flow));
			textview_yes.setText(context.getResources().getString(R.string.on_lock));
		}
	}


	public void setOnClickListener(){
		//chanel
		linear_no.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialogImpl != null) {
					if (isShotScreen) {
						dialogImpl.onShotScreenPublish(false);
					} else {
						dialogImpl.onPlayOrBackfinish(false);
					}
				}
			}
		});

		//ok
		linear_yes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(dialogImpl != null){
					if(isShotScreen){
						dialogImpl.onShotScreenPublish(true);
					}else{
						dialogImpl.onPlayOrBackfinish(true);
					}
				}
			}
		});
	}


	@Override
	public void show() {
		super.show();
		if (dialogImpl != null) {
			dialogImpl.show();
		}
	}
	@Override
	public void dismiss() {
		super.dismiss();
		if (dialogImpl != null) {
			dialogImpl.dismiss();
		}
	}



}
