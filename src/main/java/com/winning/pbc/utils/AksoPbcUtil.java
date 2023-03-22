package com.winning.pbc.utils;

import com.alibaba.fastjson.JSONException;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author j9 <czf@winning.com.cn>
 */

public class AksoPbcUtil {

    private static final String PBC_SUFFIX = ".classpath.pbc";

    private static final String MAIN_PBC_PROJECT_FILE_NAME = "pbc.main.project.setting";

    private static final FilenameFilter PBC_FILTER = new SuffixFileFilter(PBC_SUFFIX);

    public static List<File> getCopyJars4Module(String workPath,String moduleName) {
        List<File> r = new ArrayList<>();
        List<File> pbcClassPaths = getPbcClasspathFiles(workPath,moduleName);
        if (pbcClassPaths != null) {

            //TODO  如果存在多个则尝试读取配置得到单一的结果集
            Set<String> mainProjectNames = getMainProjectNames(workPath, moduleName);
            if (mainProjectNames == null || mainProjectNames.size() == 0) {
                for (File f : pbcClassPaths) {
                    r.addAll(getCopyJars4ClassPath(f.getParent(), f.getName()));
                }
            } else {
                for (File f : pbcClassPaths) {
                    Boolean skip = null;
                    if (mainProjectNames != null && mainProjectNames.size() > 0) {
                        String checkName = f.getName().substring(0, f.getName().length() - PBC_SUFFIX.length());
//                    mainProjectName.split(",")
                        for (String mainProjectName : mainProjectNames) {
                            if (checkName.equalsIgnoreCase(mainProjectName)) {
                                skip = false;
                                break;
                            }
                        }
                        if (skip == null) {
                            skip = true;
                        }
                    } else {
                        skip = false;
                    }
                    if (!skip) {
                        r.addAll(getCopyJars4ClassPath(f.getParent(), f.getName()));
                    }
                }
            }
        }
        return r;
    }



    private static Set<String> getMainProjectNames(String workPath,String moduleName) {
        File f = new File(workPath+"/"+moduleName);
        f = new File(f, MAIN_PBC_PROJECT_FILE_NAME);
        if (f.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
                String s = new String(readAll(fis), StandardCharsets.UTF_8);
                if (s != null) {
                    Set r = new HashSet();
                    String[] pss = s.split(",");
                    for (String ps : pss) {
                        if ("".equals(ps)) {
                            continue;
                        }
                        r.add(ps.trim());
                    }
                    return r;
                }
            } catch (FileNotFoundException e) {
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return null;
    }

    private static String[] getPbcClasspaths(String workPath, String moduleName) {
        File root = new File(workPath);
        String[] allPbcClassPaths= root.list(PBC_FILTER);
        if(StringUtils.isEmpty(moduleName)){
            return allPbcClassPaths;
        }
        List<String> currentPbcClassPaths=Lists.newArrayList();
        for (String pbcClassPath : allPbcClassPaths) {
            try {
                String[] jarPaths=FileUtils.readFileToString(new File(workPath,pbcClassPath),"UTF-8").split("[;:]");
                String _workPath = workPath.replace("/", "\\");
                String modulePrefix = _workPath + (_workPath.endsWith("\\") ? "" : "\\") + moduleName + "\\";
                String modulePrefix1 = modulePrefix.replace("\\", "/");
                for (String jarPath : jarPaths) {
                    if (jarPath.startsWith(modulePrefix)||jarPath.startsWith(modulePrefix1)) {
                        currentPbcClassPaths.add(pbcClassPath);
                        break;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return currentPbcClassPaths.toArray(new String[]{});
    }


    private static List<File> getPbcClasspathFiles(String workPath,String moduleName) {
        File root = new File(workPath);
        String[]  pbcClassPaths=getPbcClasspaths(workPath,moduleName);
        List<File> pbcClasspathFiles = new ArrayList<>();
        if(pbcClassPaths!=null){
            for (String pbcClassPath : pbcClassPaths) {
                pbcClasspathFiles.add(new File(root, pbcClassPath));
            }
        }
        return pbcClasspathFiles;
    }




    /**
     * 尝试清除akso4生成的临时PBC文件
     *
     * @param workPath
     */
    public static void tryCleanAkso4PbcTmpFile(String workPath) {
        for (File f : getPbcClasspathFiles(workPath,"")) {
            if (f.exists()) {
                f.delete();
            }

        }
    }

    /**
     * 是否存在PBC生成后的文件
     *
     * @param workPath
     * @return
     */
    public static boolean haveAkso4PbcFile(String workPath,String moduleName) {
        String[] pbcClassPaths = getPbcClasspaths(workPath,moduleName);
        return pbcClassPaths != null && pbcClassPaths.length > 0;
    }


    private static List<File> getCopyJars4ClassPath(String modulePath, String classpathFile) {
        File wxpbuildClasspath = new File(modulePath + File.separator + classpathFile);
        List<File> toCopyList = new ArrayList<>();
        if (wxpbuildClasspath.exists()) {
            try {
                String classPath = FileUtils.readFileToString(wxpbuildClasspath, "utf-8");

                String[] classpathArr = classPath.split(getClassPathSplitter());
                for (String s : classpathArr) {
                    File jarFile = new File(s);
                    if (jarFile.exists()) {
                        toCopyList.add(jarFile);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return toCopyList;
    }

    static String CLASS_PATH_SPLITTER = null;


    public static String getClassPathSplitter() {
        if (CLASS_PATH_SPLITTER == null) {

            String osName = System.getProperty("os.name");
            if (osName.startsWith("Mac OS")) {
                CLASS_PATH_SPLITTER = ":";
            } else if (osName.startsWith("Windows")) {
                // windows
                CLASS_PATH_SPLITTER = ";";
            } else {
                // unix or linux
                CLASS_PATH_SPLITTER = ":";
            }
        }
        return CLASS_PATH_SPLITTER;
    }


    public static byte[] readAll(InputStream is) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            byte[] bs = new byte[2048];
            while (true) {
                int len = is.read(bs, 0, bs.length);
                if (len < 0) {
                    return buf.toByteArray();
                }
                buf.write(bs, 0, len);
            }
        } catch (Exception var4) {
            throw new JSONException("read string from reader error", var4);
        } finally {
            try {
                buf.close();
            } catch (IOException e) {
            }
        }
    }

}
