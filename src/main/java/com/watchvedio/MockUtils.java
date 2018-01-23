package com.watchvedio;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockUtils {
    private static String loginUrl = "https://cas.xjtu.edu.cn/login?service=http://xjtudj.edu.cn/pcweb/cas.jsp";
    private static String userName = "";
    private static String passWord = "";


    /**
     * 模拟登陆获取获得cookie
     *
     * @return cookie字符串
     */
    public static String getCookiesStr(HttpClient httpClient) {
        PostMethod postMethod = new PostMethod(loginUrl);
        NameValuePair[] data = {new NameValuePair("uname", userName)
                , new NameValuePair("password", passWord)
                , new NameValuePair("lt", "LT-199519-9OnMWS3DfPbuuBePWaPsoLVBItJbeT")
                , new NameValuePair("execution", "e13s1")
                , new NameValuePair("_eventId", "submit")
                , new NameValuePair("execution", "登录")
        };
        postMethod.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        postMethod.setRequestHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
        postMethod.setRequestHeader("Connection", "keep-alive");
        postMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
        postMethod.setRequestBody(data);
        try {
            // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            httpClient.executeMethod(postMethod);
            String text = new String(postMethod.getResponseBodyAsString().getBytes("utf-8"));
            // 获得登陆后的 Cookie
            Cookie[] cookies = httpClient.getState().getCookies();
            StringBuffer tmpcookies = new StringBuffer();
            for (Cookie c : cookies) {
                tmpcookies.append(c.toString() + ";");
            }
            return tmpcookies.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    /**
     * 获得页面内容
     *
     * @param httpClient httpclient
     * @param url        url
     * @param cookie     cookie
     * @return 页面内容
     */
    private static String getContent(HttpClient httpClient, String url, String cookie) {
        String content = "";
        GetMethod getMethod = new GetMethod(url);
        getMethod.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        getMethod.setRequestHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
        getMethod.setRequestHeader("Connection", "keep-alive");
        getMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
        getMethod.setRequestHeader("Cookie", cookie);
        try {
            httpClient.executeMethod(getMethod);
            content = new String(getMethod.getResponseBodyAsString().getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 获得页面内容
     *
     * @param httpClient httpclient
     * @param cookie     cookie
     */
    private static void commitVideoWathTime(HttpClient httpClient, String cookie, String watchTime, String courseID, String ccID, String classId) {
        String url = "http://xjtudj.edu.cn/course/course_updateUserWatchRecord.do";
        PostMethod postMethod = new PostMethod(url);
        // 设置登陆时要求的信息，用户名和密码
        NameValuePair[] data = {new NameValuePair("courseID", courseID)
                , new NameValuePair("ccID", ccID)
                , new NameValuePair("classID", classId)
                , new NameValuePair("watchTime", watchTime)
        };
        postMethod.setRequestHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        postMethod.setRequestHeader("Accept-Encoding", "gzip, deflate");
        postMethod.setRequestHeader("Accept-Language", "h-CN,zh;q=0.9,en;q=0.8");
        //postMethod.setRequestHeader("Connection", "keep-alive");
        //postMethod.setRequestHeader("Content-Length", "45");
        postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        postMethod.setRequestHeader("Host", "xjtudj.edu.cn");
        postMethod.setRequestHeader("Origin", "http://xjtudj.edu.cn");
        postMethod.setRequestHeader("Referer", "http://xjtudj.edu.cn/course/course_detail.do?ccID=742&cateID=54&courseID=1240&classID=49");
        postMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
        postMethod.setRequestHeader("Cookie", cookie);
        postMethod.setRequestBody(data);
        try {
            httpClient.executeMethod(postMethod);
            String text = new String(postMethod.getResponseBodyAsString().getBytes("utf-8"));
            System.out.println(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据dom视频url
     *
     * @param content 内容
     * @return 视频url
     */
    private static List<String> getHrefStringList(String content) {
        List<String> list = new ArrayList<>();
        Elements links = Jsoup.parse(content).select("a");
        for (Element element : links) {
            if (element.attr("href").contains("classID")) {
                System.out.println(element.attr("href"));
                list.add(element.attr("href"));
            }
        }
        return list;
    }

    /**
     * 自动观看视频
     *
     * @param classIdUrl 课程列表url
     */
    private static void watchVideo(String classIdUrl, String cookie) {
        String ccId = "";
        String courseID = "";
        String classID = "";
        HttpClient httpClient = new HttpClient();
        String content = getContent(httpClient, classIdUrl, cookie);
        List<String> hrefList = getHrefStringList(content);
        for (String href : hrefList) {
            System.out.println(href);
            Pattern classIdpattern = Pattern.compile("&classID=.*");
            Matcher classIdMatcher = classIdpattern.matcher(href);
            if (classIdMatcher.find()) {
                classID = classIdMatcher.group(0).replace("&classID=", "");
            }

            Pattern ccIdpattern = Pattern.compile("ccID=.*&cateID");
            Matcher ccIdMatcher = ccIdpattern.matcher(href);
            if (ccIdMatcher.find()) {
                ccId = ccIdMatcher.group(0).replace("&cateID", "").replace("ccID=", "");
            }

            Pattern courseIDpattern = Pattern.compile("courseID=.*&classID");
            Matcher courseIDMatcher = courseIDpattern.matcher(href);
            if (courseIDMatcher.find()) {
                courseID = courseIDMatcher.group(0).replace("&classID", "").replace("courseID=", "");
            }

            System.out.println("ccId:" + ccId + ",courseID:" + courseID + ",classID:" + classID);

            for (int i = 0; i < 5000; i = i + 10) {
                commitVideoWathTime(httpClient, cookie, String.valueOf(i), courseID, ccId, classID);
            }
        }
    }

    public static void main(String[] args) {
        HttpClient httpClient = new HttpClient();
        //String cookie = getCookiesStr(httpClient);
        //System.out.println("------------------------------"+cookie);
        String cookie = "route=; JSESSIONID=24C29B4DF041783EB539CBCF8370F8E8";
        watchVideo("http://xjtudj.edu.cn/myzone/zone_newStudyPlanDetail.do?classID=49", cookie);
    }
}
