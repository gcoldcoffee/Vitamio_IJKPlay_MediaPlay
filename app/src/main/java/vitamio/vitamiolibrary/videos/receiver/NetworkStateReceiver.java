package vitamio.vitamiolibrary.videos.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.net.NetworkInterface;

import vitamio.vitamiolibrary.videos.utils.Alog;
import vitamio.vitamiolibrary.videos.utils.NetWorkUtils;

/**
 * 网络状态
 * @author Administrator
 *
 */

public class NetworkStateReceiver extends BroadcastReceiver 
{

	private static final String TAG="**监听网络状态**";
	
	private NetworkStateListener networkStateListener;
	private NetworkInterface interfaceCache;
	
	public NetworkStateReceiver(NetworkStateListener networkStateListener){
		
		this.networkStateListener = networkStateListener;
	}

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		boolean networkState= NetWorkUtils.isNetworkConnected(context);

		Alog.i(TAG, "**网络是否连接**" + networkState);

		boolean isWifiEnabled=NetWorkUtils.isWifiConnected(context);
		Alog.i(TAG,"**wifi是否连接**"+isWifiEnabled);

		boolean isMobledEnabled=NetWorkUtils.isMobileConnected(context);
		Alog.i(TAG,"**moble是否连接**"+isMobledEnabled);

		if(networkStateListener!=null){
			if(networkState){
				networkStateListener.onNetworkEnabled();

				if(isWifiEnabled){
					networkStateListener.onNetWifiEnabled();
				}else{
					networkStateListener.onNetWifiDisabled();
				}
				if(isMobledEnabled){
					networkStateListener.onNetMobleEnabled();
				}else{
					networkStateListener.onNetMobleDisabled();
				}
			}else {
				networkStateListener.onNetworkDisabled();
			}

		}
	}

	public interface NetworkStateListener 
	{
		void onNetworkEnabled();

		void onNetworkDisabled();

		void onNetWifiEnabled();

		void onNetWifiDisabled();

		void onNetMobleEnabled();

		void onNetMobleDisabled();
	}

	
}
