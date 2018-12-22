package com.htmlUnit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class App {
  private Set<String> urls = new HashSet<>();
  private String baseurl = "http://www.dongfengye.com";
  private String contentUrl = "http://www.dongfengye.com/d/101410.html";
  // 书名
  private String title = "限量的你";
  private String path = "E:/" + title + ".txt";

  public static void main(String[] args) {
    try {
      System.out.println("--begin--");
      new App().run();
      System.out.println("--end--");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void run() throws Exception {
    File file = new File(path);
    if (file.exists() && !file.delete()) {
      System.out.println("无法删除文件" + path);
    }
    WebClient webClient = new WebClient();
    // 是否开启css渲染
    webClient.getOptions().setCssEnabled(false);
    // 是否开启js渲染
    webClient.getOptions().setJavaScriptEnabled(false);
    // 是否允许所有人链接(解决https证书不信任问题)
    webClient.getOptions().setUseInsecureSSL(true);
    // js失败是否抛出异常
    webClient.getOptions().setThrowExceptionOnScriptError(false);
    // 是否启用重定向
    webClient.getOptions().setRedirectEnabled(true);



    // 获取目录页面
    HtmlPage page;
    page = webClient.getPage(contentUrl);
    List<HtmlAnchor> anchors = page.getAnchors();
    anchors.stream()
    .filter(this::filter)
    .sorted(Comparator.comparing(this::getChapterUrl))
    .forEach(htmlAnchor -> {
      HtmlPage childPage = null;
      // 章节标题
      String chapterTitle = getChapterTitle(htmlAnchor);
      System.out.println(chapterTitle);
      StringBuilder sb = new StringBuilder();
      sb.append(chapterTitle);
      try {
        String url = baseurl + htmlAnchor.getHrefAttribute();
        if (!urls.contains(htmlAnchor.getHrefAttribute())) {
          urls.add(htmlAnchor.getHrefAttribute());
          childPage = webClient.getPage(url);
          HtmlElement htmlContent = childPage.getHtmlElementById("content");
          // 章节内容
          String chapterContent = htmlContent.asText();
          chapterContent = updateString(chapterContent);
          sb.append(chapterContent);
        } else {
          System.out.println("重复章节--" + chapterTitle);
          //sb.append("重复章节--" + chapterTitle);
        }
      } catch (Exception e) {
        System.out.println("无法打开章节--" + chapterTitle);
        //sb.append("无法打开章节--" + chapterTitle);
      }
      try {
        writeStringToFile(sb.toString(), path);
      } catch (Exception e) {
        System.out.println("无法写入章节--" + chapterTitle);
        e.printStackTrace();
      }
    });
  }

  private String getChapterTitle(HtmlAnchor htmlAnchor) {
    String chapterTitle = htmlAnchor.asText();
    chapterTitle = chapterTitle.replaceAll("[^0-9a-zA-Z\u4e00-\u9fa5.，,。？“”]+", "");
    chapterTitle = chapterTitle.substring(chapterTitle.indexOf("第")+1);
    return chapterTitle;
  }
  
  private int getChapterUrl(HtmlAnchor htmlAnchor) {
    String url = htmlAnchor.getHrefAttribute();
     url =url.substring(url.lastIndexOf("/")+1,url.length()-5);
     return Integer.parseInt(url);
  }
  
  private boolean filter(HtmlAnchor htmlAnchor) {
    try {
      String url = htmlAnchor.getHrefAttribute();
      url =url.substring(url.lastIndexOf("/")+1,url.length()-5);
       Integer.parseInt(url);
       return true;
    } catch (Exception e) {
     return false;
    }
  }

  private void writeStringToFile(String str, String filePath) throws IOException {
    try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filePath,true)),true)) {
      pw.write(str);
    }
  }

  private String updateString(String str) {
    return str.replace("</p>", "").replace("</ter>热门推荐", "");
  }
}
