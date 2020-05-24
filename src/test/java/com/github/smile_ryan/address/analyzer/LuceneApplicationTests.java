package com.github.smile_ryan.address.analyzer;

import com.github.smile_ryan.address.analyzer.common.util.AddressUtils;
import com.github.smile_ryan.address.analyzer.service.LuceneService;
import com.google.common.base.Splitter;
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
			List<String> addressSpits = Splitter.on(" ").omitEmptyStrings().splitToList(line);
			Document doc = new Document();
			String regionCode = addressSpits.get(0);
			String regionName = addressSpits.get(1);
			String parentCode = addressSpits.get(2);
			String regionPath = addressSpits.get(4);
			int regionLevel = Splitter.on(",").splitToList(regionPath).size();
			doc.add(new StringField("RegionCode", regionCode, Store.YES));
			doc.add(new TextField("RegionName", regionName, Store.YES));
			doc.add(new TextField("ShortName", AddressUtils.extractShortName(regionName), Store.YES));
			doc.add(new StringField("ParentCode", parentCode, Store.YES));
			doc.add(new IntPoint("RegionLevel", regionLevel));
			doc.add(new StoredField("RegionLevel", regionLevel));
			doc.add(new NumericDocValuesField("RegionLevel", regionLevel));
			doc.add(new StringField("RegionPath", regionPath, Store.YES));
			luceneService.addDocument(doc);
		}

		luceneService.optimize();
	}

}
