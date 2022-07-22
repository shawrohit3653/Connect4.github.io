package com.Game.Connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import  javafx.geometry.Point2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	private static final int COLUMNS=7;
	private static final int ROWS=6;
	private static final int CIRCLE_DIAMETER=80;

	private static final String discColor1="#24303E";
	private static final String discColor2="#4CAA88";

	private static String PLAYER_ONE="Player One";
	private static String PLAYER_TWO="Player Two";

private boolean isPlayerOneTurn=true;
private Disc[][] insertedDiscArray= new Disc[ROWS][COLUMNS]; //for Structural changes



	@FXML
	public GridPane rootGridPane;

	@FXML
	public Pane InsertedDiscPane;

	@FXML
	public Label playerNameLabel;

	@FXML
	private  boolean isAllowedToInsert=true; //flag to avoid same color disc beaing added

	@FXML
	public TextField playerOneTextField,playerTwoTextField;

	@FXML
	public Button setNamesButton;

	public  void createPlayground()
	{
		Shape rectangleWithHoles=createGameStructuralGrid();
		rootGridPane.add(rectangleWithHoles,0,1);

		List<Rectangle> rectangleList=createClickableColumns();
		for(Rectangle rectangle:rectangleList)
		{
			rootGridPane.add(rectangle,0,1);

		}
		setNamesButton.setOnAction(event -> {
			PLAYER_ONE = playerOneTextField.getText();
			PLAYER_TWO = playerTwoTextField.getText();
		});
	}

	private  Shape createGameStructuralGrid()
	{
		Shape rectangleWithHoles= new Rectangle((COLUMNS+1)*CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);

		for(int row=0;row<ROWS;row++)
		{
			for (int col=0;col<COLUMNS;col++)
			{
				Circle circle=new Circle();
				circle.setRadius(CIRCLE_DIAMETER/2);
				circle.setCenterX(CIRCLE_DIAMETER/2);
				circle.setCenterY(CIRCLE_DIAMETER/2);
				circle.setSmooth(true);

				circle.setTranslateX(col*(CIRCLE_DIAMETER + 5)+ CIRCLE_DIAMETER/4);
				circle.setTranslateY(row*(CIRCLE_DIAMETER + 5)+ CIRCLE_DIAMETER/4);
				rectangleWithHoles=Shape.subtract(rectangleWithHoles,circle);
			}
		}

		rectangleWithHoles.setFill(Color.WHITE);
		return rectangleWithHoles;
	}

	private List<Rectangle> createClickableColumns(){

		List<Rectangle> rectangleList=new ArrayList<>();
		for(int col=0;col<COLUMNS;col++)
		{
			Rectangle rectangle=new Rectangle(CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);
			rectangle.setFill((Color.TRANSPARENT));
			rectangle.setTranslateX(col*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);

			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.TRANSPARENT));

			final int column=col;
			rectangle.setOnMouseClicked(event -> {
				if(isAllowedToInsert)
				{
					isAllowedToInsert=false;// When disc is bring dropped then no more disc will be inserted
					insertDisc(new Disc(isPlayerOneTurn),column);
				}

			});
			rectangleList.add(rectangle);
		}
		return rectangleList;
	}
private void insertDisc(Disc disc,int column)
{
	int row=ROWS-1;
	while(row>=0)
	{
		if(getDiscIfPresent(row,column)==null)
			break;
		row--;
	}

	if(row<0)
		return;

	insertedDiscArray[row][column]=disc;
	InsertedDiscPane.getChildren().add(disc);

	disc.setTranslateX(column*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);

	int currentRow=row;
	TranslateTransition translateTransition=new TranslateTransition(Duration.seconds(0.5),disc);
	translateTransition.setToY(row*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);
	translateTransition.setOnFinished(event -> {

		isAllowedToInsert=true; // finally, When disc is dropped allow next player to insert disc.
		if(gameEnded(currentRow,column))
		{
		gameOver();
		return;
		}
		isPlayerOneTurn=!isPlayerOneTurn;
		playerNameLabel.setText(isPlayerOneTurn?PLAYER_ONE:PLAYER_TWO);
	});

	translateTransition.play();
}

	private boolean gameEnded(int row, int column) {

		//Vertical Points: A small example:Player has inserted his disc at row=2,column=3
		//range of row value =0,1,2,3,4,5
		//index of exah element present in column [row][column]:0,3 1,3 2,3 3,3 4,3 5,3

		List<Point2D> verticalPoints =	IntStream.rangeClosed(row-3,row+3)
				.mapToObj(r->new Point2D(r,column))
				.collect(Collectors.toList());

		List<Point2D> HorizontalPoints =	IntStream.rangeClosed(column-3,column+3)
				.mapToObj(col -> new Point2D(row,col))
				.collect(Collectors.toList());

		Point2D startPoint1=new Point2D(row-3,column+3);
		List<Point2D> diagonalPoints =	IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint1.add(i,-i))
				.collect(Collectors.toList());

		Point2D startPoint2=new Point2D(row-3,column-3);
		List<Point2D> diagonal2Points =	IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint2.add(i,i))
				.collect(Collectors.toList());

		boolean isEnded=checkCombinations(verticalPoints)|| checkCombinations(HorizontalPoints)
				|| checkCombinations(diagonalPoints) || checkCombinations(diagonal2Points);

		return isEnded;
	}

	private boolean checkCombinations(List <Point2D> points)
	{
		int chain=0;
		for (Point2D point:points) {

			int rowIndexFOrArray=(int)point.getX();
			int columnIndexFOrArray=(int)point.getY();

			Disc disc=getDiscIfPresent(rowIndexFOrArray,columnIndexFOrArray);
			if(disc!=null && disc.isPlayerOneMove==isPlayerOneTurn) {
				chain++;
				if (chain == 4) {
					return true;
				}
			}else {
					chain=0;
				}
			}
			return false;
		}


private  Disc getDiscIfPresent(int row,int column){
		if(row>=ROWS || row<0|| column>=COLUMNS ||column<0)
		{
			return null;
		}
		return insertedDiscArray[row][column];
}

private void gameOver()
{
	String winner=isPlayerOneTurn?PLAYER_ONE:PLAYER_TWO;
	System.out.println("Winner is:"+ winner);

	Alert alert=new Alert(Alert.AlertType.INFORMATION);
	alert.setTitle("ConnectFour");
	alert.setHeaderText("The Winner is" + winner);
	alert.setContentText("Want to play again");


	ButtonType yesBtn=new ButtonType("Yes");
	ButtonType noBtn=new ButtonType("No,Exit");
	alert.getButtonTypes().setAll(yesBtn,noBtn);

	Platform.runLater(()->{
		Optional<ButtonType> btnCLicked=alert.showAndWait();
		if(btnCLicked.isPresent() && btnCLicked.get()==yesBtn)
		{//..user chose No..so reset the game
			resetGame();
		}
		else {
			//..user chose No..so Exit the game
			Platform.exit();
			System.exit(0);
		}
	});
}

	public void resetGame() {
		InsertedDiscPane.getChildren().clear();

		for(int row=0;row<insertedDiscArray.length;row++)
		{
			for(int col=0; col< insertedDiscArray[row].length;col++)
			{
				insertedDiscArray[row][col]=null;
			}
		}
		isPlayerOneTurn=true;
		playerNameLabel.setText(PLAYER_ONE); // create new playground

		createPlayground();
	}

	private  static  class Disc extends Circle{
		private  final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove)
		{
			this.isPlayerOneMove=isPlayerOneMove;
			setRadius((CIRCLE_DIAMETER/2));
			setFill(isPlayerOneMove? Color.valueOf(discColor1):Color.valueOf(discColor2));
			setCenterX(CIRCLE_DIAMETER/2);
			setCenterY(CIRCLE_DIAMETER/2);
		}
}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
