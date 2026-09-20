import java.lang.reflect.Method;
import org.springframework.ai.ollama.api.OllamaChatOptions;

public class Reflect {
    public static void main(String[] args) throws Exception {
        for (Method m : OllamaChatOptions.builder().getClass().getMethods()) {
            if (m.getName().toLowerCase().contains("tool") || m.getName().toLowerCase().contains("func")) {
                System.out.println(m.getName() + " " + m.getParameterTypes()[0].getName());
            }
        }
    }
}
