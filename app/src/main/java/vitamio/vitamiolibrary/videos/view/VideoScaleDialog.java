package vitamio.vitamiolibrary.videos.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import vitamio.vitamiolibrary.R;
import vitamio.vitamiolibrary.videos.mediaimpl.VPImpl;

/**
 * Dialog工具类,方便获取项目中所用到的各式Dialog,并包含显示与关闭Dialog常用方法
 */
@SuppressLint("InlinedApi")
public class VideoScaleDialog extends Dialog {

	public Context context;

	public VPImpl.ScaleImpl dialogImpl;

	public Button btn_fs, btn_sn, btn_fulls,btn_os;



	public VideoScaleDialog(Context context, VPImpl.ScaleImpl dialogImpl) {
		super(context,  R.style.weixin_dialog);
		this.context = context;
		this.dialogImpl = dialogImpl;
		initView();
	}

	public VideoScaleDialog(Context context, int theme) {
		super(context, 0);
	}


	public void initView() {
		View dialogView = View.inflate(context, R.layout.vp_scale_dialog,null);
		btn_fs=(Button)dialogView.findViewById(R.id.btn_fs);
		btn_sn=(Button)dialogView.findViewById(R.id.btn_sn);
		btn_fulls=(Button)dialogView.findViewById(R.id.btn_fulls);
		btn_os=(Button)dialogView.findViewById(R.id.btn_os);

		setContentView(dialogView);

		setOnClickListener();


	}




	public void setOnClickListener(){
		//chanel
		btn_fs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialogImpl != null) {
					dialogImpl.onScale(3);
				}
			}
		});
		btn_sn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialogImpl != null) {
					dialogImpl.onScale(2);
				}
			}
		});
		btn_fulls.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialogImpl != null) {
					dialogImpl.onScale(1);
				}
			}
		});
		btn_os.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialogImpl != null) {
					dialogImpl.onScale(0);
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
		setCanceledOnTouchOutside(true);
	}
	@Override
	public void dismiss() {
		super.dismiss();
		if (dialogImpl != null) {
			dialogImpl.dismiss();
		}
	}



}
