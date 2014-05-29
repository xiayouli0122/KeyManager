package com.yuri.mykey.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class ZipUtil {
	
	private static final String ZIP_PW = "loveyou1314gmy";
	
	/**
	 * use zip4j.jar password zip
	 * 
	 * @param file zip file
	 * @param srcFile source file
	 */
	public static void zipForPw(File file, File srcFile) {
		try {
			ZipFile zipFile = new ZipFile(file);
			ZipParameters zipParameters = new ZipParameters();
			zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			zipParameters.setEncryptFiles(true);
			zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
			zipParameters.setPassword(ZIP_PW);
			zipFile.addFile(srcFile, zipParameters);
		} catch (ZipException e) {
			e.printStackTrace();
		}
	}

	/**
	 * use zip4j.jar unzip by special pw
	 * 
	 * @param path
	 */
	public static boolean unZipForPw(String path) {
		try {
			ZipFile zipFile = new ZipFile(path);

			if (zipFile.isEncrypted()) {
				zipFile.setPassword(ZIP_PW);
			} else {
				return false;
			}

			File file = new File(XmlUtil.SAVE_PATH);
			if (!file.exists()) {
				file.mkdirs();
			}

			zipFile.extractAll(XmlUtil.SAVE_PATH);
		} catch (ZipException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * 压缩文件 （对多个文件和文件夹进行压缩</br>
	 * 压缩方法使用的是可变参数</br>
	 * 可以压缩到多个文件，可以写数组的方式或一个个写到参数列表里
	 * 
	 * @param zip
	 *            表示 压缩后生成的zip文件 例如：new File("/root/abc/111.zip")
	 * @param srcFiles
	 *            表示 被压缩的文件（可以是多个
	 *            可以利用ArrayList的toArray()来提供参数，也可以利用数组，对于单个文件则直接传进来即可
	 * @throws IOException
	 */
	public static void ZipFiles(File zip, File... srcFiles) throws IOException {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
		ZipFiles(out, "backup", srcFiles);
		out.close();
	}

	private static void ZipFiles(ZipOutputStream out, String path,
			File... srcFiles) {
		path = path.replaceAll("\\*", "/");
		if (!path.endsWith("/")) {
			path += "/";
		}
		byte[] buf = new byte[1024];
		try {
			for (int i = 0; i < srcFiles.length; i++) {
				if (srcFiles[i].isDirectory()) {
					File[] files = srcFiles[i].listFiles();
					String srcPath = srcFiles[i].getName();
					srcPath = srcPath.replaceAll("\\*", "/");
					if (!srcPath.endsWith("/")) {
						srcPath += "/";
					}
					out.putNextEntry(new ZipEntry(path + srcPath));
					ZipFiles(out, path + srcPath, files);
				} else {
					FileInputStream in = new FileInputStream(srcFiles[i]);
					out.putNextEntry(new ZipEntry(path + srcFiles[i].getName()));
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					out.closeEntry();
					in.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
