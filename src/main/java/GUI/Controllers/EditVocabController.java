package GUI.Controllers;

import GUI.Entities.Vocabulary;
import GUI.Services.Data.IDataService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EditVocabController implements Initializable {
    private Vocabulary vocabulary;
    private IDataService<Vocabulary> dataService;
    @FXML
    public WebView webView;
    @FXML
    public TextField wordTextField;
    @FXML
    public TextArea meaningTextArea;
    @FXML
    public Button saveButton;
    @FXML
    public Button goBackButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webView.setFontScale(1.2);
        meaningTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            webView.getEngine().loadContent(meaningTextArea.getText());
        });
    }

    public void saveButton_OnClicked(ActionEvent actionEvent) throws IOException {
        vocabulary.setWord(wordTextField.getText());
        vocabulary.setDetailString(meaningTextArea.getText().replace("\n", ""));
        dataService.update(vocabulary.getId(), vocabulary);
        MainController.getMainController().navigateToSearchVocabView();
    }

    public void goBackButton_OnClicked(ActionEvent actionEvent) throws IOException {
        MainController.getMainController().navigateToSearchVocabView();
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
        wordTextField.setText(vocabulary.getWord());
        meaningTextArea.setText(reformatHtml(vocabulary.getDetailString()));
        webView.getEngine().loadContent(vocabulary.getDetailString());
    }

    private String reformatHtml(String detailString) {
        Document doc = Jsoup.parseBodyFragment(vocabulary.getDetailString());
        return doc.body().children().toString();
    }

    public void setDataService(IDataService<Vocabulary> dataService) {
        this.dataService = dataService;
    }
}
