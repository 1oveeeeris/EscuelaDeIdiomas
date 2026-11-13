import java.util.ArrayList;

// ============================================================
// INTERFACES
// ============================================================

// Lo que TODAS las ventas tienen en comun (servicios y examen)
interface Venta {
    String getFolio();      // identificador unico
    String getAlumno();     // nombre del alumno
    String getTipo();       // "Taller", "Curso regular", etc.
    double calcularCobro(); // cada tipo calcula su propio precio
}

// Lo que SOLO algunas ventas pueden hacer: cancelarse.
// El curso intensivo NO la implementa, por eso no se puede cancelar.
interface Cancelable {
    int diasMinimos();            // dias de anticipacion que se exigen
    double porcentajeReembolso(); // 0.70 = devuelve el 70 %
}

// ============================================================
// EXCEPCION PROPIA DEL NEGOCIO
// Para para distinguir una cancelacion fuera de plazo de
// cualquier otro error del programa.
// ============================================================
class CancelacionFueraDePlazoException extends Exception {
    CancelacionFueraDePlazoException(int requeridos, int disponibles) {
        // super(...) manda el mensaje a la clase Exception
        super("Cancelacion rechazada: se requerian " + requeridos
            + " dias y habia " + disponibles);
    }
}

// ============================================================
// CLASE BASE (HERENCIA)
// Guarda lo que comparten los tres servicios: folio y alumno.
// Es "abstract" porque nunca se crea un "Servicio" a secas,
// solo sus hijos (CursoRegular, CursoIntensivo, Taller).
// ============================================================
abstract class Servicio implements Venta {
    // private = nadie fuera de esta clase puede tocarlos
    // final = una vez asignados, ya no cambian
    private final String folio;
    private final String alumno;

    // Constructor: valida los datos ANTES de crear el objeto.
    // Si algo esta mal, lanza excepcion y el objeto nunca existe.
    protected Servicio(String folio, String alumno) {
        this.folio = texto(folio, "El folio");
        this.alumno = texto(alumno, "El alumno");
    }

    // Revisa que un texto no sea nulo ni este vacio.
    // Es static para poder usarlo tambien desde ExamenCertificacion.
    static String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " no puede estar vacio");
        }
        return valor.trim(); // quita espacios sobrantes
    }

    // Revisa que un numero este entre un minimo y un maximo
    static int rango(int valor, int min, int max, String campo) {
        if (valor < min || valor > max) {
            throw new IllegalArgumentException(campo + " debe estar entre " + min
                + " y " + max + " (recibido: " + valor + ")");
        }
        return valor;
    }

    // Getters: unica forma de leer los datos privados
    @Override public String getFolio() { return folio; }
    @Override public String getAlumno() { return alumno; }
}

// ============================================================
// CLASES HIJAS
// "extends Servicio" = hereda folio y alumno
// "implements Cancelable" = se puede cancelar
// ============================================================

// Curso regular: $1,800 por mes, 10 % de descuento si son 3 meses o mas
class CursoRegular extends Servicio implements Cancelable {
    private final int meses;

    CursoRegular(String folio, String alumno, int meses) {
        super(folio, alumno);                          // usa el constructor de la base
        this.meses = rango(meses, 1, 6, "Los meses");  // meses de 1 a 6
    }

    @Override public String getTipo() { return "Curso regular"; }

    @Override public double calcularCobro() {
        double total = meses * 1800;
        if (meses >= 3) {
            total = total * 0.90;   // descuento del 10 %
        }
        return total;
    }

    // Se cancela con 7 dias o mas y devuelve el 70 %
    @Override public int diasMinimos() { return 7; }
    @Override public double porcentajeReembolso() { return 0.70; }
}

// Curso intensivo: $3,200 por mes, +$400 de materiales (una sola vez).
// NO implementa Cancelable, asi que no se puede cancelar.
class CursoIntensivo extends Servicio {
    private final int meses;
    private final boolean conMateriales;

    CursoIntensivo(String folio, String alumno, int meses, boolean conMateriales) {
        super(folio, alumno);
        this.meses = rango(meses, 1, 3, "Los meses");  // meses de 1 a 3
        this.conMateriales = conMateriales;
    }

    @Override public String getTipo() { return "Curso intensivo"; }

    @Override public double calcularCobro() {
        double total = meses * 3200;
        if (conMateriales) {
            total = total + 400;    // se suma una sola vez, no por mes
        }
        return total;
    }
}

// Taller: $250 por sesion
class Taller extends Servicio implements Cancelable {
    private final int sesiones;

    Taller(String folio, String alumno, int sesiones) {
        super(folio, alumno);
        this.sesiones = rango(sesiones, 1, 12, "Las sesiones");  // de 1 a 12
    }

    @Override public String getTipo() { return "Taller"; }
    @Override public double calcularCobro() { return sesiones * 250; }

    // Se cancela con 3 dias o mas y devuelve el 100 %
    @Override public int diasMinimos() { return 3; }
    @Override public double porcentajeReembolso() { return 1.0; }
}

// ============================================================
// EXAMEN DE CERTIFICACION
// NO hereda de Servicio (es un tramite externo, no una
// inscripcion), pero SI implementa Venta y Cancelable.
// Por eso puede ir en la misma lista que los demas.
// ============================================================
class ExamenCertificacion implements Venta, Cancelable {
    private final String folio;
    private final String alumno;
    private final String idioma;
    private final int intento;

    // Como no hereda de Servicio, valida sus propios datos
    ExamenCertificacion(String folio, String alumno, String idioma, int intento) {
        this.folio = Servicio.texto(folio, "El folio");
        this.alumno = Servicio.texto(alumno, "El alumno");
        this.idioma = Servicio.texto(idioma, "El idioma");
        if (intento < 1) {
            throw new IllegalArgumentException("El intento debe ser 1 o mayor (recibido: " + intento + ")");
        }
        this.intento = intento;
    }

    @Override public String getFolio() { return folio; }
    @Override public String getAlumno() { return alumno; }
    @Override public String getTipo() { return "Examen certificacion"; }

    // $950, con 20 % de descuento del segundo intento en adelante
    @Override public double calcularCobro() {
        double total = 950;
        if (intento >= 2) {
            total = total * 0.80;
        }
        return total;
    }

    // Se cancela con 5 dias o mas y devuelve el 50 %
    @Override public int diasMinimos() { return 5; }
    @Override public double porcentajeReembolso() { return 0.50; }
}

// ============================================================
// CAJA
// Maneja TODAS las ventas en una sola lista (ArrayList<Venta>).
// Nunca pregunta "de que clase es"; solo usa lo que Venta ofrece.
// ============================================================
class Caja {
    private final ArrayList<Venta> ventas = new ArrayList<>();

    // Agrega una venta, pero rechaza folios repetidos
    void registrar(Venta venta) {
        if (buscar(venta.getFolio()) != null) {
            throw new IllegalArgumentException("Folio repetido: " + venta.getFolio());
        }
        ventas.add(venta);
    }

    // Busca por folio; si no existe devuelve null
    Venta buscar(String folio) {
        for (Venta v : ventas) {
            if (v.getFolio().equals(folio)) {
                return v;
            }
        }
        return null;
    }

    // Arma el texto de una venta (se reutiliza en varios reportes)
    private String linea(Venta v) {
        return String.format("%-5s | %-22s | %-8s | $%,10.2f",
            v.getFolio(), v.getTipo(), v.getAlumno(), v.calcularCobro());
    }

    // Imprime todos los servicios y el total del dia
    void imprimirLista() {
        System.out.println("--- Lista de servicios ---");
        for (Venta v : ventas) {
            System.out.println(linea(v));
        }
        System.out.printf("Total del dia: $%,.2f%n", total());
    }

    // Suma el cobro de todas las ventas
    double total() {
        double suma = 0;
        for (Venta v : ventas) {
            suma += v.calcularCobro();
        }
        return suma;
    }

    // Con UN solo recorrido calcula: cuantos hay de cada tipo
    // y cual tiene el mayor cobro.
    void imprimirResumen() {
        // Dos listas paralelas: tipos.get(i) tiene su cantidad en cantidades.get(i)
        ArrayList<String> tipos = new ArrayList<>();
        ArrayList<Integer> cantidades = new ArrayList<>();
        Venta mayor = null;

        for (Venta v : ventas) {
            // Contar por tipo
            int posicion = tipos.indexOf(v.getTipo());
            if (posicion == -1) {
                // Tipo nuevo: lo agrego con cantidad 1
                tipos.add(v.getTipo());
                cantidades.add(1);
            } else {
                // Tipo ya visto: le sumo 1
                cantidades.set(posicion, cantidades.get(posicion) + 1);
            }

            // Buscar el mayor cobro
            if (mayor == null || v.calcularCobro() > mayor.calcularCobro()) {
                mayor = v;
            }
        }

        System.out.println("--- Servicios por tipo ---");
        for (int i = 0; i < tipos.size(); i++) {
            System.out.println(tipos.get(i) + ": " + cantidades.get(i));
        }
        if (mayor != null) {
            System.out.println("Mayor cobro: " + linea(mayor));
        }
    }

    // Regla del plazo, escrita UNA sola vez.
    // Si no se cumplen los dias minimos, lanza la excepcion del negocio.
    // Si si se cumplen, devuelve cuanto se reembolsa.
    private double calcularReembolso(Venta venta, int diasFaltantes)
            throws CancelacionFueraDePlazoException {
        Cancelable cancelable = (Cancelable) venta; // la tratamos como cancelable
        if (diasFaltantes < cancelable.diasMinimos()) {
            throw new CancelacionFueraDePlazoException(cancelable.diasMinimos(), diasFaltantes);
        }
        return venta.calcularCobro() * cancelable.porcentajeReembolso();
    }

    // Cancela un servicio por folio
    void cancelar(String folio, int diasFaltantes) {
        System.out.println(">> Cancelar " + folio + " con " + diasFaltantes + " dias de anticipacion");

        // 1) El folio existe?
        Venta venta = buscar(folio);
        if (venta == null) {
            System.out.println("   No existe el folio " + folio);
            return;
        }

        // 2) Es cancelable? (el curso intensivo no)
        if (!(venta instanceof Cancelable)) {
            System.out.println("   " + folio + " no es cancelable (" + venta.getTipo() + ")");
            return;
        }

        // 3) Esta dentro del plazo?
        try {
            double reembolso = calcularReembolso(venta, diasFaltantes);
            ventas.remove(venta); // solo se elimina si no hubo excepcion
            System.out.printf("   Cancelado. Reembolso: $%,.2f%n", reembolso);
        } catch (CancelacionFueraDePlazoException e) {
            System.out.println("   " + e.getMessage());
        }
    }

    // Calcula cuanto se reembolsaria si todo lo cancelable se cancelara
    // con X dias de anticipacion. NO modifica la lista.
    double reembolsoSimulado(int diasFaltantes) {
        double total = 0;
        for (Venta v : ventas) {
            if (v instanceof Cancelable) {
                try {
                    total += calcularReembolso(v, diasFaltantes);
                } catch (CancelacionFueraDePlazoException e) {
                    // Fuera de plazo: no suma nada, solo avisa
                    System.out.println("   (sin reembolso para " + v.getFolio() + ": " + e.getMessage() + ")");
                }
            }
        }
        return total;
    }
}

// ============================================================
// PROGRAMA PRINCIPAL
// Cada registro va en su propio try/catch: si uno tiene datos
// malos, se muestra el mensaje y el programa sigue con el siguiente.
// ============================================================
public class Main {
    public static void main(String[] args) {
        Caja caja = new Caja();

        System.out.println("==== REGISTRO ====");

        // ---- 8 servicios validos (2 de cada tipo) ----
        try { caja.registrar(new CursoRegular("R1", "Ana", 3)); }
        catch (IllegalArgumentException e) { System.out.println("Rechazado: " + e.getMessage()); }

        try { caja.registrar(new CursoRegular("R2", "Luis", 2)); }
        catch (IllegalArgumentException e) { System.out.println("Rechazado: " + e.getMessage()); }

        try { caja.registrar(new CursoIntensivo("I1", "Sol", 2, true)); }
        catch (IllegalArgumentException e) { System.out.println("Rechazado: " + e.getMessage()); }

        try { caja.registrar(new CursoIntensivo("I2", "Marco", 1, false)); }
        catch (IllegalArgumentException e) { System.out.println("Rechazado: " + e.getMessage()); }

        try { caja.registrar(new Taller("T1", "Rosa", 5)); }
        catch (IllegalArgumentException e) { System.out.println("Rechazado: " + e.getMessage()); }

        try { caja.registrar(new Taller("T2", "Pedro", 10)); }
        catch (IllegalArgumentException e) { System.out.println("Rechazado: " + e.getMessage()); }

        try { caja.registrar(new ExamenCertificacion("E1", "Eva", "Ingles", 2)); }
        catch (IllegalArgumentException e) { System.out.println("Rechazado: " + e.getMessage()); }

        try { caja.registrar(new ExamenCertificacion("E2", "Juan", "Frances", 1)); }
        catch (IllegalArgumentException e) { System.out.println("Rechazado: " + e.getMessage()); }

        // ---- 2 registros con datos invalidos ----
        try { caja.registrar(new CursoRegular("R3", "Karla", 7)); }   // 7 meses: fuera de rango
        catch (IllegalArgumentException e) { System.out.println("Rechazado: " + e.getMessage()); }

        try { caja.registrar(new Taller("T3", "   ", 4)); }           // alumno vacio
        catch (IllegalArgumentException e) { System.out.println("Rechazado: " + e.getMessage()); }

        // ---- 1 folio repetido ----
        try { caja.registrar(new Taller("T1", "Diana", 2)); }         // T1 ya existe
        catch (IllegalArgumentException e) { System.out.println("Rechazado: " + e.getMessage()); }

        // ---- Reportes ----
        System.out.println();
        caja.imprimirLista();
        System.out.println();
        caja.imprimirResumen();

        // ---- 4 cancelaciones ----
        System.out.println("\n==== CANCELACIONES ====");
        caja.cancelar("R1", 10);    // aceptada
        caja.cancelar("T2", 1);     // fuera de plazo (se exigen 3 dias)
        caja.cancelar("I1", 10);    // tipo no cancelable
        caja.cancelar("X99", 10);   // folio inexistente

        // ---- Lista y total despues de cancelar ----
        System.out.println();
        caja.imprimirLista();

        // ---- Simulacion: no modifica la lista ----
        System.out.println("\n==== REEMBOLSO SIMULADO (10 dias) ====");
        System.out.printf("Se reembolsaria en total: $%,.2f%n", caja.reembolsoSimulado(10));
        System.out.printf("Total sin cambios: $%,.2f%n", caja.total());
    }
}