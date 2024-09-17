package com.example.cahtbotprimerosauxilios;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private Interpreter tflite;
    EditText input;
    private RecyclerView messageRecycler;
    private MessageListAdapter messageAdapter;
    private List<String> wordsToKeep;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        input=findViewById(R.id.inputText);
        messageRecycler = (RecyclerView) findViewById(R.id.ChatRecycler);
        messageAdapter = new MessageListAdapter(this, new ArrayList<>());
        messageRecycler.setLayoutManager(new LinearLayoutManager(this));
        messageRecycler.setAdapter(messageAdapter);
        try {
            InputStream inputStream = this.getAssets().open("model.tflite");
            byte[] modelBytes = new byte[inputStream.available()];
            inputStream.read(modelBytes);
            ByteBuffer buffer = ByteBuffer.allocateDirect(modelBytes.length).order(ByteOrder.nativeOrder());
            tflite = new Interpreter(buffer.put(modelBytes));
            Toast.makeText(this,"Modelo cargado", Toast.LENGTH_SHORT).show();

            wordsToKeep=new ArrayList<>();
            InputStream inputStreamWK = this.getAssets().open("words_to_keep.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStreamWK));
            String palabra;
            while ((palabra = reader.readLine()) != null) {
                wordsToKeep.add(palabra);
            }
            Toast.makeText(this,"Words to keep cargado", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this,"no se ha cargado el modelo",Toast.LENGTH_SHORT).show();
        }

    }

    public void procesarInput(View v){
        String in=input.getText().toString();
        if (in.split(" ").length > 100){
            Toast.makeText(this,"Límite máximo de palabras es 100",Toast.LENGTH_SHORT).show();
        }else {
            messageAdapter.addItem(new Message(in, 0));
            StringBuilder inMod=new StringBuilder();
            Arrays.stream(in.split(" "))
                    .filter((palabra)->wordsToKeep.contains(palabra))
                    .forEach((palabra)->inMod.append(palabra).append(" "));

            Toast.makeText(this,inMod.toString().toLowerCase(),Toast.LENGTH_SHORT).show();

            String[][] entrada = new String[][]{{inMod.toString().toLowerCase()}};
            float[][] output = new float[1][7];
            tflite.run(entrada, output);

            float mayor = output[0][0];
            int indice = 0;
            for (int i = 1; i < 7; i++) {
                if (output[0][i] > mayor) {
                    mayor = output[0][i];
                    indice = i;
                }
            }
            String texto = "";
            switch (indice) {
                case 0:
                    texto = "Quemadura: \n - Refresque la quemadura con agua por al menos 10 minutos.\n- Evita que el agua apunte directamente a la quemadura.\n- No aplique hielo.\n" +
                            "- Si no está adherido a la piel quitese la ropa que esté cerca de la quemadura.\n- Si la quemadura tiene ampollas déjalas intáctas.\n- Puedes cubrir la herida con un plástico o un vendaje húmedo.\n\nPor último:\n- Si se trata de una quemadura solar aplica loción.\n- Si se trata de una quemadura química en los ojos enjuague y aplique Dipotherine";
                    break;
                case 1:
                    texto = "Cortes y rasguños:\n- Si la herida sangra de manera abundante, aplique presión sobre la herida para detener el sangrado.\n- Una vez que el sangrado se detenga limpia la herida con agua limpia.\n- Puedes usar desinfectante para limpiar la herida asegurnadote de usar compresas estériles al limpiar.";
                    break;
                case 2:
                    texto = "Mordedura o picadura de insecto:\n- Tranquiliza a la persona y aconséjale que no se rasque en el lugar de la picadura.\n- Si el aguijón sigue en la persona retíralo lo antes posible.\n- Al extraerlo no uses pinzas o dedos, en vez de eso rasque suavemente la zona con un objeto plano para eliminarlo.\n- Limpia la herida y enfría la zona.";
                    break;
                case 3:
                    texto = "Ampolla:\n- Limpia la piel sobre y alrededor de la ampolla con agua tibia.\n- Usa una aguja estéril estándar para realizar dos perforaciones sobre la esquina inferiror de la ampolla.\n- Empuja cuidadosamente el líquido fuera hasta elimnarlo.\n- Limpia y cubre con un vendaje aséptico.";
                    break;
                case 4:
                    texto = "Mareo:\nDetente si es posible. Si no es posible detener el viaje mira hacia el frente a un punto fijo en el horizonte. Si es posible date aire fresco y respira profundamente y lentamente";
                    break;
                case 5:
                    texto = "Fiebre:\n- Humdece a la persona con agua tibia usando una esponja, no use agua fría ya que puede causar malestar y hacer que su cuerpo no libere calor.\n- Sigue controlando los síntomas y presta atención por si aparecer otro síntoma";
                    break;
                case 6:
                    texto = "Dificultad respiratoria:\n- Ayuda a la persona a colocarse en una posición cómoda y tranquilícela.\n- Si es necesaria medicación ayudela y afloje toda la ropa ajustada.\n- Quédate con la persona hasta que respire normalmente.";
                    break;
            }
            messageAdapter.addItem(new Message(texto, 1));
            input.setText("");
        }
    }
}