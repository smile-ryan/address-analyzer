package com.github.smile_ryan.address.analyzer;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.github.smile_ryan.address.analyzer.common.model.Address;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hankcs.hanlp.HanLP;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.LoggerFactory;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * <pre>
 * 名称：TestCase
 * 描述：TestCase.java
 * </pre>
 *
 * @author <a href="mailto:smile.ryan@outlook.com">Ryan Chen</a>
 * @since v1.0.0
 */
@Slf4j
public class TestCase {

    static {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
            configurator.doConfigure("/Users/ryan/dev/workspace/address-analyzer/src/test/resources/logback-spring.xml");
        } catch (JoranException e) {
            e.printStackTrace();
        }
    }

    private String[] unit = new String[]{"自治区", "自治州", "自治县", "自治旗", "特别行政区", "行政区", "地区", "省", "市", "县", "盟", "区", "乡", "镇", "街道"};
    private String[] minzu = new String[]{"壮族", "满族", "回族", "苗族", "维吾尔族", "土家族", "彝族", "蒙古族", "藏族", "布依族", "侗族", "瑶族", "朝鲜族", "白族", "哈尼族", "哈萨克族", "黎族", "傣族", "畲族", "傈僳族", "仡佬族",
        "东乡族", "高山族", "拉祜族", "水族", "佤族", "纳西族", "羌族", "土族", "仫佬族", "锡伯族", "柯尔克孜族", "达斡尔族", "景颇族", "毛南族", "撒拉族", "布朗族", "塔吉克族", "阿昌族", "普米族", "鄂温克族", "怒族", "京族", "基诺族", "德昂族",
        "保安族", "俄罗斯族", "裕固族", "乌兹别克族", "门巴族", "鄂伦春族", "独龙族", "塔塔尔族", "赫哲族", "珞巴族"};

    @Test
    public void spitCsv() throws IOException {
        String inFilePath = "/Users/ryan/Downloads/tb_blc_order_info.csv";
        String outFilePath = "/Users/ryan/Downloads/tb_blc_order_info_simple.csv";

        File outFile = new File(outFilePath);
        if (outFile.exists()) {
            System.out.println("文件已存在，是否删除成功：" + outFile.delete());
        }
        int ignoreLineNum = 100;
        int splitLineNum = 900;
        BufferedReader reader = new BufferedReader(new FileReader(new File(inFilePath)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
        String line;
        int i = 0;
        while (StringUtils.isNotBlank(line = reader.readLine())) {
            if (i >= splitLineNum) {
                break;
            }
            if (i++ > ignoreLineNum) {
                System.out.println(line);
                writer.write(line);
                writer.newLine();
            }
        }
        writer.close();
        reader.close();
    }

    @Test
    public void tokenize() throws IOException {
        String text = "河南省,郑州市,中牟县,姚家镇校庄村,云水一路,程子航,18800001111";
        Analyzer analyzer = new IKAnalyzer(true);
        TokenStream stream = analyzer.tokenStream("", new StringReader(text));
        CharTermAttribute cta = stream.addAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            System.out.print(cta.toString() + "    ");
        }
        System.out.println();

        System.out.println(HanLP.segment(text));

    }

    @Test
    public void analyzeAddress() throws IOException {
        String file = "/Users/ryan/Downloads/tb_blc_order_info_simple.csv";
        BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
        String line;
        while (StringUtils.isNotBlank(line = reader.readLine())) {
            List<String> list = Splitter.on("\",\"").omitEmptyStrings().trimResults().splitToList(line);
            String address = list.get(1);
            System.out.println(address);
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet("http://localhost:8080/address/analyze?address=" + URLEncoder.encode(address));
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                List<Address> t = new Gson().fromJson(EntityUtils.toString(responseEntity), new TypeToken<List<Address>>() {}.getType());
                t.stream().map(a -> a.toString().replaceAll("\\d{6,}\\|", "")).peek(System.out::println).count();
            }
            System.out.println("--------------------------------------------------------------------------");
        }
    }

    @Test
    public void analyzeAddress2() throws IOException {
        String file = "/Users/ryan/dev/workspace/address-analyzer/src/main/resources/address.txt";
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        List<String> list = Lists.newLinkedList();
        while (StringUtils.isNotBlank(line = reader.readLine())) {
            List<String> addressSpits = Splitter.on(" ").omitEmptyStrings().splitToList(line);
            String regionName = addressSpits.get(1).trim();

            list.add(regionName);
            String regionName2 = regionName;
            if (regionName2.contains("族")) {
                for (String m : minzu) {
                    regionName2 = regionName2.replaceAll(m, "");
                }
            }
            if (regionName2.length() >= 3) {
                for (String m : unit) {
                    if (regionName2.endsWith(m)) {
                        regionName2 = regionName2.replaceAll(m, "");
                        break;
                    }
                }
            }
            list.add(regionName2);


        }
        list.stream().distinct().peek(System.out::println).collect(Collectors.toList());
    }


    @Test
    public void search() throws Exception {
//        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "stdout");

        System.out.println(System.getProperty("org.apache.commons.logging.Log"));;
        String indexDir = "/Users/ryan/tmp/lucene/address";
        String field = "ShortName";
        String queryStr = "新疆省";
        Directory directory = FSDirectory.open(Paths.get(indexDir));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new WhitespaceAnalyzer();
        QueryParser parser = new QueryParser(field, analyzer);
        Query query = parser.parse(queryStr);
        TopDocs docs = searcher.search(query, 10);
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println(doc);
        }
        reader.close();
    }

}
