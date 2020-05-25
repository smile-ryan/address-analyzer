package com.github.smile_ryan.address.analyzer;

import com.github.smile_ryan.address.analyzer.common.util.AddressUtils;
import com.github.smile_ryan.address.analyzer.service.LuceneService;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.classification.features.IFeatureWeighter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

@SpringBootTest
public class LuceneApplicationTests {

	@Autowired
	private LuceneService luceneService;

	@Value("classpath:address.txt")
	private Resource addressResource;

	@Test
	public void initAddressIndex() throws IOException {
		luceneService.deleteAll();

		File file = addressResource.getFile();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while (StringUtils.isNotBlank(line = reader.readLine())) {
			List<String> addressSpits = Splitter.on("\t").splitToList(line);
			if (addressSpits.size() != 6) {
				continue;
			}
			Document doc = new Document();
			String countryCode = addressSpits.get(0);
			String regionName = addressSpits.get(1);
			String regionCode = addressSpits.get(2);
			String parentCode = addressSpits.get(3);
			String regionPath = addressSpits.get(4).replaceAll("\\s", ",");
			String regionScheme = addressSpits.get(5);
			List<String> splitToList = Splitter.on(",").omitEmptyStrings().splitToList(regionPath);
			doc.add(new StringField("CountryCode", countryCode, Store.YES));
			doc.add(new StringField("RegionCode", regionCode, Store.YES));


			doc.add(new TextField("RegionName", regionName, Store.YES));

			if (countryCode.equals("CN") || countryCode.equals("TW")) {
				doc.add(new TextField("RegionNameCN", HanLP.convertToSimplifiedChinese(regionName), Store.YES));
				doc.add(new TextField("ShortName", AddressUtils.extractShortName(HanLP.convertToSimplifiedChinese(regionName)), Store.YES));
			}

			if (StringUtils.isNotEmpty(parentCode)) {
				doc.add(new StringField("ParentCode", parentCode, Store.YES));
			}
			doc.add(new IntPoint("RegionLevel", splitToList.size()));
			doc.add(new StoredField("RegionLevel", splitToList.size()));
			doc.add(new NumericDocValuesField("RegionLevel", splitToList.size()));
			String value = Joiner.on(",").join(splitToList);
			if (value.contains("010,110000")) {
				value = value.replace("010,110000", "110000,110100");
			} else if (value.contains("021,310000")) {
				value = value.replace("021,310000", "310000,310100");
			} else if (value.contains("022,120000")) {
				value = value.replace("022,120000", "120000,120100");
			} else if (value.contains("023,500000")) {
				value = value.replace("023,500000", "500000,500100");
			}
			doc.add(new StringField("RegionPath", value, Store.YES));
			doc.add(new StringField("RegionScheme", regionScheme, Store.YES));

			luceneService.addDocument(doc);
		}
		luceneService.commit(true);
		luceneService.optimize();
	}

}
