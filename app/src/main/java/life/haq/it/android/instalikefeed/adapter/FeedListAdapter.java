package life.haq.it.android.instalikefeed.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import life.haq.it.android.instalikefeed.R;
import life.haq.it.android.instalikefeed.app.FeedImageView;
import life.haq.it.android.instalikefeed.data.FeedItem;
import life.haq.it.android.instalikefeed.volley.AppController;

//import com.nostra13.universalimageloader.core.ImageLoader;

public class FeedListAdapter extends BaseAdapter {
	private Activity activity;
	private LayoutInflater inflater;
	private List<FeedItem> feedItems;
	ImageLoader imageLoader = AppController.getInstance().getImageLoader();
	private DisplayImageOptions options;
	private int feedType;


	public FeedListAdapter(Activity activity, List<FeedItem> feedItems) {
		this.activity = activity;
		this.feedItems = feedItems;
	}

	@Override
	public int getCount() {
		return feedItems.size();
	}

	@Override
	public Object getItem(int location) {
		return feedItems.get(location);
	}

	@Override
	public long getItemId(int position) { return position; }

	@Override
	public int getViewTypeCount() { return 2; }

	@Override
	public int getItemViewType(int position) {
		FeedItem item = feedItems.get(position);

		feedType = 0;
		return feedType;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int feedType = getItemViewType(position);
		if (inflater == null)
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_feed, null);
		}
		if (imageLoader == null)
			imageLoader = AppController.getInstance().getImageLoader();

		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.explore_icon)
				.showImageOnFail(R.drawable.explore_icon)
				.resetViewBeforeLoading(true)
				.cacheOnDisk(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300))
				.build();

		FeedItem item = feedItems.get(position);

			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView timestamp = (TextView) convertView
					.findViewById(R.id.timestamp);

			ImageView profilePic = (ImageView) convertView.findViewById(R.id.profilePic);
			FeedImageView feedImageView = (FeedImageView) convertView.findViewById(R.id.feedImage1);


			name.setText(item.getName());
			//set id in tag attribute of textview
			name.setTag(item.getId());
			//set time
			timestamp.setText(item.getTimeStamp());

			// user profile pic
			//profilePic.setImageUrl(item.getProfilePic(), imageLoader);
			com.nostra13.universalimageloader.core.ImageLoader.getInstance()
					.displayImage(item.getProfilePic(), profilePic, options, new SimpleImageLoadingListener());
			//profilePic.setTag(item.getProfilePic());

			// Feed image
			if (item.getImge() != null) {
				feedImageView.setImageUrl(item.getImge(), imageLoader);
				//ImageLoader.getInstance().displayImage(item.getImge(), feedImageView, options, new SimpleImageLoadingListener());
				feedImageView.setVisibility(View.VISIBLE);
				feedImageView.setResponseObserver(new FeedImageView.ResponseObserver() {
					@Override
					public void onError() {
					}

					@Override
					public void onSuccess() {
					}
				});
			} else {
				feedImageView.setVisibility(View.GONE);
			}

		return convertView;
	}

}