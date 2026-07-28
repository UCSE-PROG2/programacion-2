package funcional;

/**
 * Punto de entrada. Ejecuta todos los ejemplos del material de Unidad 4.
 * Cada clase Demo corresponde a una sección del documento:
 *   01-programacion-funcional-java.md
 *
 * Para correr desde IntelliJ: clic derecho en Main → Run 'Main.main()'
 * Para correr desde terminal:  ./gradlew run
 */
public class Main {

    public static void main(String[] args) {
        LambdasDemo.demo();
        InterfacesFuncionalesDemo.demo();
        MethodReferencesDemo.demo();
        OptionalDemo.demo();
        StreamApiDemo.demo();
        CollectorsDemo.demo();
        ImperativoVsFuncionalDemo.demo();
        EjerciciosResueltos.demo();
    }
}
