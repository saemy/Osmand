package net.osmand.plus.sherpafy;

import java.io.File;

import net.osmand.IndexConstants;
import net.osmand.map.OsmandRegions;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.download.DownloadActivityType;
import net.osmand.plus.download.IndexItem;
import android.content.Context;

public class TourDownloadType extends DownloadActivityType {
	
	public static final TourDownloadType TOUR = new TourDownloadType(R.string.download_tours, "tour");

	public TourDownloadType(int resource, String... tags) {
		super(resource, tags);
	}
	
	public boolean isAccepted(String fileName) {
		return true;
	}
	
	public File getDownloadFolder(OsmandApplication ctx, IndexItem indexItem) {
		return ctx.getAppPath(IndexConstants.TOURS_INDEX_DIR);
	}
	
	public boolean isZipStream(OsmandApplication ctx, IndexItem indexItem) {
		return true;
	}
	
	public boolean isZipFolder(OsmandApplication ctx, IndexItem indexItem) {
		return true;
	}
	
	public boolean preventMediaIndexing(OsmandApplication ctx, IndexItem indexItem) {
		return true;
	}
	
	public String getUnzipExtension(OsmandApplication ctx, IndexItem indexItem) {
		return "";
	}
	
	public String getUrlSuffix(OsmandApplication ctx) {
		return "&tour=yes";
	}

	public String getVisibleDescription(IndexItem indexItem, Context ctx) {
		return "";
	}
	
	public String getVisibleName(IndexItem indexItem, Context ctx, OsmandRegions osmandRegions) {
		return getBasename(indexItem) + "\n" + indexItem.getDescription();
	}
	
	public String getTargetFileName(IndexItem item) {
		return item.getBasename();
	}

}
