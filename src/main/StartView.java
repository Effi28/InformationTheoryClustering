package main;

import java.io.File;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import other.Start;

public class StartView extends Application {
	private Stage stage;
	private Button btnOpen;
	private Button btnStart;
	private ChoiceBox<String> cbAlgo;
	private TextField txtAmountCl;
	private FileChooser fileChooser;
	private VBox vBox;
	private File file;
	private TextField txtParam = null;
	private Button btnOk;

	@Override
	public void start(Stage primaryStage) {
		initialObjects(primaryStage);
		setListener();
		addToVBox();

		Scene scene = new Scene(vBox, 200, 200);

		primaryStage.setTitle("Spectral Clustering");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void addToVBox() {
		vBox.getChildren().add(btnOpen);
		vBox.getChildren().add(cbAlgo);
		vBox.getChildren().add(txtAmountCl);
		vBox.getChildren().add(btnStart);

	}

	private void setListener() {
		btnOpen.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				file = fileChooser.showOpenDialog(stage);
				if (file != null) {
					openFile();
				}
			}
		});

		btnOk.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				Stage stage = (Stage) btnOk.getScene().getWindow();
				stage.hide();
				if (cbAlgo.getValue().equals("KNN")) {
					other.Parameters.setK(Integer.valueOf(txtParam.getText()));
				} else {
					other.Parameters
							.setEPSILON(Double.valueOf(txtParam.getText()));

				}
			}

		});

		btnStart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				// Start start = new Start();
				try {
					if (checkStartConditions()) {
						other.Parameters.setALGORITHM(cbAlgo.getValue());
						other.Parameters.setDATASET(file);
						other.Parameters.setNUMCLUSTERS(
						Integer.valueOf(txtAmountCl.getText()));
						Thread t = new Thread(new Start());
						t.start();
					} else {
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		cbAlgo.getSelectionModel().selectedIndexProperty()
				.addListener(new ChangeListener<Number>() {
					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue, Number newValue) {
						if (newValue.intValue() == 1) {
							Stage stage = new Stage();
							VBox vBox = new VBox();
							txtParam = new TextField(("K"));

							vBox.getChildren().add(txtParam);
							vBox.getChildren().add(btnOk);

							stage.setTitle("Select parameter k for KNN");
							stage.setScene(new Scene(vBox, 100, 100));
							stage.show();
						}
						if (newValue.intValue() == 2) {
							Stage stage = new Stage();
							VBox vBox = new VBox();
							txtParam = new TextField(("E"));

							vBox.getChildren().add(txtParam);
							vBox.getChildren().add(btnOk);

							stage.setTitle(
									"Select parameter epsilon for ERANGE");
							stage.setScene(new Scene(vBox, 100, 100));
							stage.show();
						}
					}
				});
		txtAmountCl.lengthProperty()
				.removeListener(new ChangeListener<Number>() {

					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue, Number newValue) {

						if (newValue.intValue() > oldValue.intValue()) {
							char ch = txtAmountCl.getText()
									.charAt(oldValue.intValue());
							System.out.println("Length:" + oldValue + "  "
									+ newValue + " " + ch);

							// Check if the new character is the number or
							// other's
							if (!(ch >= '0' && ch <= '9')) {

								// if it's not number then just setText to
								// previous one
								txtAmountCl.setText(
										txtAmountCl.getText().substring(0,
												txtAmountCl.getText().length()
														- 1));
							}
						}
					}

				});
	}

	private boolean checkStartConditions() {
		if (file != null && cbAlgo.getValue() != null
				&& txtAmountCl.getText() != null) {
			return true;
		} else {
			return false;
		}
	}

	private void initialObjects(Stage primaryStage) {
		stage = primaryStage;
		vBox = new VBox();
		fileChooser = new FileChooser();
		if(new File("/evaluation datasets").exists()){
			fileChooser.setInitialDirectory(new File(
					("/evaluation datasets")));			
		}
		btnOpen = new Button("Choose a arff Dataset");
		txtAmountCl = new TextField("Amount of Clusters");
		cbAlgo = new ChoiceBox<String>(FXCollections.observableArrayList("MDL",
				"KNN", "ERANGE", "KNNMDL"));
		btnStart = new Button("Start");
		btnOk = new Button("OK");

		btnOpen.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		txtAmountCl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		cbAlgo.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		btnStart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		btnOk.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
	}

	public static void main(String[] args) {
		launch(args);
	}

	private void openFile() {
		String s = file.toString()
				.substring(file.toString().lastIndexOf(File.separator) + 1);
		btnOpen.setText(s);
		other.Parameters.setDATASET(file);
	}
}