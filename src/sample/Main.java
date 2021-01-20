package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class Main extends Application {
    
    ConcreteMediatorForIndividuals concreteMediatorForIndividuals = new ConcreteMediatorForIndividuals();
    volatile boolean isLockedSimulation = false;
    ReentrantLock reentrantLockForMediator = new ReentrantLock();
    private final ScheduledExecutorService mediatorNextStepScheduler = Executors.newScheduledThreadPool(1);
    Runnable mediatorNextStepRunnable;
    private final ExecutorService pauseButtonService = Executors.newFixedThreadPool(3);
    Runnable pauseButtonRunnable;
    private final ExecutorService continueButtonService = Executors.newFixedThreadPool(3);
    Runnable continueButtonRunnable;
    private final ExecutorService addButtonsService = Executors.newFixedThreadPool(5);
    Runnable addIndOneByOneRunnable;
    Runnable addIndsInBulkRunnable;
    private final ExecutorService nextStepService = Executors.newFixedThreadPool(1);
    Text text = new Text();
    
    
    /**
     * Shuts down executer services before it is closed.
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        mediatorNextStepScheduler.shutdown();
        pauseButtonService.shutdown();
        continueButtonService.shutdown();
        addButtonsService.shutdown();
        nextStepService.shutdown();
        concreteMediatorForIndividuals.exit();
    }
    
    /**
     * Creates the canvas and buttons needed. Calls nextStepForMediator every second.
     * @param primaryStage the stage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        primaryStage.setTitle("Epidemic Simulation Test");
        Group root = new Group();
        Canvas canvas = new Canvas(1000, 700);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        
        // borders of the canvas
        Line line1 = new Line(0, 599, 999, 599);
        line1.setStroke(Color.BLACK);
        Line line2 = new Line(999, 0, 999, 599);
        line2.setStroke(Color.BLACK);
        root.getChildren().addAll(line1, line2);
        // borders of the canvas
        
        text.setFont(new Font(15));
//		text.setWrappingWidth(20);
        text.setTextAlignment(TextAlignment.JUSTIFY);
        text.setText(" ");
        text.setX(0);
        text.setY(660);
        
        Button pauseButton = createButton("pause");
        Button continueButton = createButton("continue");
        Button addIndOneButton = createButton("oneByOne");
        Button addIndsInBulkButton = createButton("inBulk");
        
        root.getChildren().add(canvas);
        root.getChildren().add(addIndOneButton);
        root.getChildren().add(addIndsInBulkButton);
        root.getChildren().add(pauseButton);
        root.getChildren().add(continueButton);
        root.getChildren().add(text);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        
        mediatorNextStepRunnable = new Runnable() {
            public void run() {
                if (!getIsLockedSimulation()){
                    nextStepForMediator(gc);
                }
            }
        };
        
        mediatorNextStepScheduler.scheduleAtFixedRate(mediatorNextStepRunnable, 1, 1, TimeUnit.SECONDS);
        
    }
    
    /**
     * Gets the individuals from the mediator and prints them in the canvas.
     * Calls the play method of the mediator for the next step.
     * @param gc the graphic context
     */
    private void nextStepForMediator(GraphicsContext gc){
        
        List<Individual> listIndividuals;
        int numberOfDead;
        
        reentrantLockForMediator.lock();
        try {
            listIndividuals = concreteMediatorForIndividuals.getIndividuals();
            numberOfDead = concreteMediatorForIndividuals.getNumberOfDead();
        }
        finally {
            reentrantLockForMediator.unlock();
        }
        
        nextStepService.submit(new Runnable() {
            @Override
            public void run() {
                reentrantLockForMediator.lock();
                try {
                    concreteMediatorForIndividuals.play();
                }
                finally {
                    reentrantLockForMediator.unlock();
                }
            }
        });
        
        int numberOfInds = listIndividuals.size();
        int counterForInfected = 0;
        int counterForHealthy = 0;
        int counterForHospitalized = 0;
        
        gc.clearRect(0,0,ConcreteMediatorForIndividuals.maxXPosition+5,ConcreteMediatorForIndividuals.maxYPosition+5);
        
        for(int m = 0; m < numberOfInds; ++m) {
            
            int i;
            int j;
            
            if( listIndividuals.get(m).isInfected() )
                ++counterForInfected;
            if( listIndividuals.get(m).isHospitalized() )
                ++counterForHospitalized;
            
            i = listIndividuals.get(m).getPositionX();
            j = listIndividuals.get(m).getPositionY();
            
            Color color;
            if(listIndividuals.get(m).isInfected())
                color = Color.RED;
            else
                color = Color.GREEN;
            
            if(!listIndividuals.get(m).isHospitalized() && !listIndividuals.get(m).isDead()){
                gc.setFill(color);
                gc.fillRect(i,j,Individual.size,Individual.size);
            }
            
        }
        counterForHealthy = numberOfInds - counterForInfected - counterForHospitalized;
        text.setText("Healthy : " + counterForHealthy + " , Infected : " + counterForInfected + " , Hospitalized : " + counterForHospitalized + " , Dead : " + numberOfDead);
        
    }
    
    /**
     * Sets the isLockedSimulation.
     * @param bl true to pause the simulation.
     */
    private synchronized void setIsLockSimulation(boolean bl){
        isLockedSimulation = bl;
    }
    
    /**
     * Returns the isLockedSimulation.
     * @return the isLockedSimulation.
     */
    private synchronized boolean getIsLockedSimulation(){
        return isLockedSimulation;
    }
    
    /**
     * Creates buttons and runnable objects.
     * @param buttonName name of the button.
     * @return
     */
    private Button createButton(String buttonName){
        Button button = null;
        
        if(buttonName.equals("pause")){
            
            button = new Button("Pause");
            button.setLayoutX(340);
            button.setLayoutY(600);
            button.setMinSize(100,30);
            button.setMaxSize(100,30);
            
            pauseButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    setIsLockSimulation(true);
                    System.out.println("pause button pressed");
                }
            };
            
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    pauseButtonService.submit(pauseButtonRunnable);
                }
            });
        }
        if(buttonName.equals("continue")){
            button = new Button("Continue");
            button.setLayoutX(460);
            button.setLayoutY(600);
            button.setMinSize(100,30);
            button.setMaxSize(100,30);
            
            continueButtonRunnable = new Runnable() {
                @Override
                public void run() {
                    setIsLockSimulation(false);
                    System.out.println("continue button pressed");
                }
            };
            
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    continueButtonService.submit(continueButtonRunnable);
                }
            });
            
        }
        if(buttonName.equals("oneByOne")){
            button = new Button("Create an Individual");
            button.setLayoutX(0);
            button.setLayoutY(600);
            button.setMinSize(150,30);
            button.setMaxSize(150,30);
            
            addIndOneByOneRunnable = new Runnable() {
                @Override
                public void run() {
                    reentrantLockForMediator.lock();
                    try {
                        System.out.println("Pressed Create an Individual Button");
                        concreteMediatorForIndividuals.addIndividual();
                    }
                    finally {
                        reentrantLockForMediator.unlock();
                    }
                }
            };
            
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    addButtonsService.submit(addIndOneByOneRunnable);
                }
            });
            
        }if(buttonName.equals("inBulk")){
            button = new Button("Create Individuals in Bulk");
            button.setLayoutX(170);
            button.setLayoutY(600);
            button.setMinSize(150,30);
            button.setMaxSize(150,30);
            
            addIndsInBulkRunnable = new Runnable() {
                @Override
                public void run() {
                    reentrantLockForMediator.lock();
                    try{
                        System.out.println("Pressed Create Individuals in Bulk Button");
                        concreteMediatorForIndividuals.addIndividualsInBulk();
                    }
                    finally {
                        reentrantLockForMediator.unlock();
                    }
                }
            };
            
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    addButtonsService.submit(addIndsInBulkRunnable);
                }
            });
            
        }
        
        return button;
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
    
    
}
