package com.achersoft.tdcc.tokendb;

import com.achersoft.tdcc.enums.CharacterClass;
import com.achersoft.tdcc.enums.Rarity;
import com.achersoft.tdcc.enums.Slot;
import com.achersoft.tdcc.token.TokenService;
import com.achersoft.tdcc.token.dao.Token;
import com.achersoft.tdcc.token.persistence.TokenMapper;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

public class TokenSyncServiceImpl implements TokenSyncService {

    @Override
    public void syncTokens() throws IOException {
/*

        WebClient webClient = new WebClient();
        HtmlPage myPage = webClient.getPage("https://tokendb.com/");

        Document document = Jsoup.parse(myPage.asXml());

     /*   final Document document = Jsoup
                .connect("https://tokendb.com/")

                .followRedirects(true)
                .ignoreHttpErrors(true)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                .header("Accept-Language", "*")
                .get();

        System.err.println(document);
        */
    }
}
 