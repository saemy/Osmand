package net.osmand.plus.download;

import static net.osmand.IndexConstants.BINARY_MAP_INDEX_EXT;
import static net.osmand.IndexConstants.BINARY_SRTM_MAP_INDEX_EXT;

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import net.osmand.AndroidUtils;
import net.osmand.IndexConstants;
import net.osmand.map.OsmandRegions;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.Version;
import android.content.Context;

public class DownloadActivityType {
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
	private static Map<String, DownloadActivityType> byTag = new HashMap<String, DownloadActivityType>();
	
	public static final DownloadActivityType NORMAL_FILE = new DownloadActivityType(R.string.download_regular_maps, "map");
	public static final DownloadActivityType VOICE_FILE = new DownloadActivityType(R.string.voice, "voice");
	public static final DownloadActivityType ROADS_FILE = new DownloadActivityType(R.string.download_roads_only_maps, "road_map");
	public static final DownloadActivityType HILLSHADE_FILE = new DownloadActivityType(R.string.download_srtm_maps, "srtm_map"); 
	public static final DownloadActivityType SRTM_COUNTRY_FILE = new DownloadActivityType(R.string.download_hillshade_maps, "hillshade");
	private int resource;
	private String[] tags;

	public DownloadActivityType(int resource, String... tags) {
		this.resource = resource;
		this.tags = tags;
		for(String st : tags) {
			byTag.put(st, this);
		}
	}

	public static boolean isCountedInDownloads(DownloadActivityType tp) {
		if(tp == NORMAL_FILE || tp == ROADS_FILE){
			return true;
		}
		return false;
	}

	public String getString(Context c) {
		return c.getString(resource);
	}

	public static DownloadActivityType getIndexType(String tagName) {
		return byTag.get(tagName);
	}
	
	protected static String addVersionToExt(String ext, int version) {
		return "_" + version + ext;
	}
	
	public boolean isAccepted(String fileName) {
		if (ROADS_FILE == this || NORMAL_FILE == this) {
			return fileName.endsWith(addVersionToExt(IndexConstants.BINARY_MAP_INDEX_EXT_ZIP,
					IndexConstants.BINARY_MAP_VERSION)) 
					|| fileName.endsWith(IndexConstants.EXTRA_ZIP_EXT)
					|| fileName.endsWith(IndexConstants.SQLITE_EXT);
		} else if (VOICE_FILE == this) {
			return fileName.endsWith(addVersionToExt(IndexConstants.VOICE_INDEX_EXT_ZIP, IndexConstants.VOICE_VERSION));
		} else if (SRTM_COUNTRY_FILE == this) {
			return fileName.endsWith(addVersionToExt(IndexConstants.BINARY_SRTM_MAP_INDEX_EXT_ZIP,
					IndexConstants.BINARY_MAP_VERSION));
		} else if (HILLSHADE_FILE == this) {
			return fileName.endsWith(IndexConstants.SQLITE_EXT);
		}
		return false;
	}
	
	public File getDownloadFolder(OsmandApplication ctx, IndexItem indexItem) {
		if (ROADS_FILE == this || NORMAL_FILE == this) {
			if(indexItem.fileName.endsWith(IndexConstants.SQLITE_EXT)) {
				return ctx.getAppPath(IndexConstants.TILES_INDEX_DIR);
			}
			return ctx.getAppPath(IndexConstants.MAPS_PATH);
		} else if (VOICE_FILE == this) {
			return ctx.getAppPath(IndexConstants.VOICE_INDEX_DIR);
		} else if (SRTM_COUNTRY_FILE == this) {
			return ctx.getAppPath(IndexConstants.SRTM_INDEX_DIR);
		} else if (HILLSHADE_FILE == this) {
			return ctx.getAppPath(IndexConstants.TILES_INDEX_DIR);
		}
		throw new UnsupportedOperationException();
	}
	
	public boolean isZipStream(OsmandApplication ctx, IndexItem indexItem) {
		return true;
	}
	
	public boolean isZipFolder(OsmandApplication ctx, IndexItem indexItem) {
		return this == VOICE_FILE;
	}
	
	public boolean preventMediaIndexing(OsmandApplication ctx, IndexItem indexItem) {
		return this == VOICE_FILE && indexItem.fileName.endsWith(IndexConstants.VOICE_INDEX_EXT_ZIP);
	}
	
	public String getUnzipExtension(OsmandApplication ctx, IndexItem indexItem) {
		if (NORMAL_FILE == this) {
			if (indexItem.fileName.endsWith(IndexConstants.BINARY_MAP_INDEX_EXT_ZIP)) {
				return BINARY_MAP_INDEX_EXT;
			} else if (indexItem.fileName.endsWith(IndexConstants.BINARY_MAP_INDEX_EXT_ZIP)) {
				return BINARY_MAP_INDEX_EXT;
			} else if (indexItem.fileName.endsWith(IndexConstants.EXTRA_ZIP_EXT)) {
				return IndexConstants.EXTRA_EXT;
			} else if (indexItem.fileName.endsWith(IndexConstants.SQLITE_EXT)) {
				return IndexConstants.SQLITE_EXT;
			}
		} else if (ROADS_FILE == this) {
			return "-roads" + BINARY_MAP_INDEX_EXT;
		} else if (VOICE_FILE == this) {
			return "";
		} else if (SRTM_COUNTRY_FILE == this) {
			return BINARY_SRTM_MAP_INDEX_EXT;
		} else if (HILLSHADE_FILE == this) {
			return IndexConstants.SQLITE_EXT;
		}
		throw new UnsupportedOperationException();
	}
	
	public String getUrlSuffix(OsmandApplication ctx) {
		if (this== DownloadActivityType.ROADS_FILE) {
			return "&road=yes";
		} else if (this == DownloadActivityType.SRTM_COUNTRY_FILE) {
			return "&srtmcountry=yes";
		}else if (this== DownloadActivityType.HILLSHADE_FILE) {
			return "&hillshade=yes";
		}
		return "";
	}

	public String getBaseUrl(OsmandApplication ctx, String fileName) {
		return "http://" + IndexConstants.INDEX_DOWNLOAD_DOMAIN + "/download?event=2&"
				+ Version.getVersionAsURLParam(ctx) + "&file=" + fileName;
	}


	public IndexItem parseIndexItem(Context ctx, XmlPullParser parser) {
		String name = parser.getAttributeValue(null, "name"); //$NON-NLS-1$
		if(!isAccepted(name)) {
			return null;
		}
		String size = parser.getAttributeValue(null, "size"); //$NON-NLS-1$
		String date = parser.getAttributeValue(null, "date"); //$NON-NLS-1$
		String description = parser.getAttributeValue(null, "description"); //$NON-NLS-1$
		String parts = parser.getAttributeValue(null, "parts"); //$NON-NLS-1$
		date = reparseDate(ctx, date);
		IndexItem it = new IndexItem(name, description, date, size, parts, this);
		
		return it;
	}

	protected static String reparseDate(Context ctx, String date) {
		try {
			Date d = simpleDateFormat.parse(date);
			return AndroidUtils.formatDate(ctx, d.getTime());
		} catch (ParseException e) {
			return date;
		}
	}

	public String getVisibleDescription(IndexItem indexItem, Context ctx) {
		if (this == DownloadActivityType.SRTM_COUNTRY_FILE) {
			return ctx.getString(R.string.download_srtm_maps);
		} else if (this == DownloadActivityType.ROADS_FILE) {
			return ctx.getString(R.string.download_roads_only_item);
		}
		return "";
	}
	
	private String getVoiceName(Context ctx, String basename) {
		try {
			String nm = basename.replace('-', '_').replace(' ', '_');
			if (nm.endsWith("_tts")) {
				nm = nm.substring(0, nm.length() - 4);
			}
			Field f = R.string.class.getField("lang_"+nm);
			if (f != null) {
				Integer in = (Integer) f.get(null);
				return ctx.getString(in);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return basename;
	}
	
	public String getVisibleName(IndexItem indexItem, Context ctx, OsmandRegions osmandRegions) {
		String fileName = indexItem.fileName;
		if (this == VOICE_FILE) {
			if (fileName.endsWith(IndexConstants.VOICE_INDEX_EXT_ZIP)) {
				return ctx.getString(R.string.voice) + "\n" + getVoiceName(ctx, getBasename(indexItem));
			} else if (fileName.endsWith(IndexConstants.TTSVOICE_INDEX_EXT_ZIP)) {
				return ctx.getString(R.string.ttsvoice) + "\n" + getVoiceName(ctx, getBasename(indexItem));
			}
			return getBasename(indexItem);
		}
		final String bn = getBasename(indexItem);
		final String lc = bn.toLowerCase();
		String std = getStandardMapName(ctx, lc);
		if (std != null) {
			return std;
		}
		if (bn.contains("addresses-nationwide")) {
			final int ind = bn.indexOf("addresses-nationwide");
			String downloadName = bn.substring(0, ind - 1) + bn.substring(ind + "addresses-nationwide".length());
			return osmandRegions.getLocaleName(downloadName) + 
					" "+ ctx.getString(R.string.index_item_nation_addresses);
		}

		return osmandRegions.getLocaleName(lc);
	}
	
	private String getStandardMapName(Context ctx, String basename) {
		if(basename.equals("world-ski")) {
			return ctx.getString(R.string.index_item_world_ski);
		} else if(basename.equals("world_altitude_correction_ww15mgh")) {
			return ctx.getString(R.string.index_item_world_altitude_correction);
		} else if(basename.equals("world_basemap")) {
			return ctx.getString(R.string.index_item_world_basemap);
		} else if(basename.equals("world_bitcoin_payments")) {
			return ctx.getString(R.string.index_item_world_bitcoin_payments);
		} else if(basename.equals("world_seamarks")) {
			return ctx.getString(R.string.index_item_world_seamarks);
		}
		return null;
	}
	
	public String getTargetFileName(IndexItem item) {
		String fileName = item.fileName;
		// if(fileName.endsWith(IndexConstants.VOICE_INDEX_EXT_ZIP) ||
		// fileName.endsWith(IndexConstants.TTSVOICE_INDEX_EXT_ZIP)) {
		if (this == VOICE_FILE) {
			int l = fileName.lastIndexOf('_');
			if (l == -1) {
				l = fileName.length();
			}
			String s = fileName.substring(0, l);
			return s;
		} else if (this == HILLSHADE_FILE) {
			return fileName.replace('_', ' ');
		} else if (fileName.endsWith(IndexConstants.BINARY_MAP_INDEX_EXT)
				|| fileName.endsWith(IndexConstants.BINARY_MAP_INDEX_EXT_ZIP)) {
			int l = fileName.lastIndexOf('_');
			if (l == -1) {
				l = fileName.length();
			}
			String baseNameWithoutVersion = fileName.substring(0, l);
			if (this == DownloadActivityType.SRTM_COUNTRY_FILE) {
				return baseNameWithoutVersion + IndexConstants.BINARY_SRTM_MAP_INDEX_EXT;
			}
			if (this == DownloadActivityType.ROADS_FILE) {
				baseNameWithoutVersion += "-roads";
			}
			baseNameWithoutVersion += IndexConstants.BINARY_MAP_INDEX_EXT;
			return baseNameWithoutVersion;
		} else if (fileName.endsWith(IndexConstants.SQLITE_EXT)) {
			return fileName.replace('_', ' ');
		} else if (fileName.endsWith(IndexConstants.EXTRA_ZIP_EXT)) {
			return fileName.substring(0, fileName.length() - IndexConstants.EXTRA_ZIP_EXT.length())
					+ IndexConstants.EXTRA_EXT;
		}
		return fileName;
	}
	

	public String getBasename(IndexItem indexItem) {
		String fileName = indexItem.fileName;
		if (fileName.endsWith(IndexConstants.EXTRA_ZIP_EXT)) {
			return fileName.substring(0, fileName.length() - IndexConstants.EXTRA_ZIP_EXT.length());
		}
		if (fileName.endsWith(IndexConstants.SQLITE_EXT)) {
			return fileName.substring(0, fileName.length() - IndexConstants.SQLITE_EXT.length()).replace('_', ' ');
		}
		if (this == VOICE_FILE) {
			int l = fileName.lastIndexOf('_');
			if (l == -1) {
				l = fileName.length();
			}
			String s = fileName.substring(0, l);
			return s;
		}
		int ls = fileName.lastIndexOf('_');
		if (ls >= 0) {
			return fileName.substring(0, ls);
		} else if(fileName.indexOf('.') > 0){
			return fileName.substring(0, fileName.indexOf('.'));
		}
		return fileName;
	}



}