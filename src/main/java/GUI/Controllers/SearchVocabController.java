package GUI.Controllers;

import GUI.Entities.Vocabulary;
import GUI.Services.Api.ApiCognitiveMicrosoftTextToSpeechService;
import GUI.Services.Api.ApiCognitiveMicrosoftTranslatorService;
import GUI.Services.Api.Language;
import GUI.Services.CheckInternetConnectivity;
import GUI.Services.Data.DictionaryDataService;
import GUI.Services.Data.IDataService;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class SearchVocabController implements Initializable {

    @FXML
    public TextField searchTextField;
    @FXML
    public ListView<Vocabulary> vocabularyList;
    @FXML
    public WebView webView;
    @FXML
    public HBox bottomHBox;
    @FXML
    public Button modifyButton;
    @FXML
    public Button deleteButton;
    @FXML
    public HBox topHBox;
    @FXML
    public Button pronounceButton;
    private final IDataService<Vocabulary> dataService;
    private final ApiCognitiveMicrosoftTextToSpeechService textToSpeechService;
    private Vocabulary selectedVocabulary;
    public Vocabulary getSelectedVocabulary() {
        return selectedVocabulary;
    }

    public void setSelectedVocabulary(Vocabulary selectedVocabulary) {
        this.selectedVocabulary = selectedVocabulary;
    }
    public SearchVocabController() {
        dataService = DictionaryDataService.getInstance();
        textToSpeechService = new ApiCognitiveMicrosoftTextToSpeechService();
        vocabularyList = new ListView<>();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchTerm = newValue != null ? newValue : "";
            updateVocabularyList(searchTerm);
            if (vocabularyList.getItems().size() > 0) {
                vocabularyList.getSelectionModel().select(0);
            } else {
                webView.getEngine().loadContent(String.format("<h2>Xin lỗi, từ '%s' không xuất hiện trong từ điển.</h2>", searchTerm));
            }

        });
        vocabularyList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        vocabularyList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedVocabulary = newValue;
            if (selectedVocabulary == null) {
                return;
            }
            webView.getEngine().loadContent(selectedVocabulary.getDetailString());
            webView.setFontScale(1.2);
        });
        updateVocabularyList(null);
        vocabularyList.getSelectionModel().select(0);
    }

    private void updateVocabularyList(String searchTerm) {
        vocabularyList.getItems().clear();
        if (searchTerm == null || searchTerm.equals("")) {
            vocabularyList.getItems().addAll(dataService.getWords(0, 50));
            vocabularyList.setVisible(true);
            return;
        }
        vocabularyList.getItems().addAll(dataService.findWord(searchTerm));
    }


    public void addNewVocabButton_OnClicked(ActionEvent actionEvent) throws IOException {
        MainController.getMainController().navigateToAddNewVocabView(dataService);
    }

    public void modifyButton_OnClicked(ActionEvent actionEvent) throws IOException {
        if (selectedVocabulary == null)
            return;
        MainController.getMainController().navigateToEditVocabView(selectedVocabulary, dataService);
    }

    public void deleteButton_OnClicked(ActionEvent actionEvent) {
        if (selectedVocabulary == null)
            return;
        ButtonType deleteButtonType = new ButtonType("Xóa");
        ButtonType cancelButtonType = new ButtonType("Hủy");

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "", deleteButtonType, cancelButtonType);
        confirmationAlert.setTitle("Xóa từ vựng");
        confirmationAlert.setHeaderText("Bạn có chắc muốn xóa từ '" + selectedVocabulary.getWord() + "'?");
        confirmationAlert.showAndWait();

        if (confirmationAlert.getResult() == deleteButtonType) {
            dataService.remove(selectedVocabulary.getId());
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle("Thành công");
            alert.setHeaderText("Đã xóa từ thành công");
            alert.showAndWait();
            updateVocabularyList(null);
            searchTextField.setText("");
        }
    }

    public void pronounceButton_OnClicked(MouseEvent mouseEvent) {
        if (selectedVocabulary == null)
            return;
        if (!CheckInternetConnectivity.IsConnected()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Đã có lỗi xảy ra khi thực hiện phát âm, hãy thử kiểm tra kết nối internet.", ButtonType.OK);
            alert.setTitle("Error.");
            alert.setHeaderText("Đã có lỗi xảy ra.");
            alert.showAndWait();
            return;
        }
        Thread thread = new Thread(() -> {
            try {
                textToSpeechService.textToSpeech(selectedVocabulary.getWord(), Language.English);
            } catch (ExecutionException | InterruptedException e) {
                // ignore
            }
        });
        thread.start();
    }
}
