import java.sql.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner teclado = new Scanner(System.in);

        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String user = "RIBERA";
        String password = "ribera";

        System.out.println("Introduce el numero de la etapa:");
        int numeroEtapa = teclado.nextInt();
        teclado.nextLine();

        System.out.println("Introduce el origen de la etapa:");
        String origen = teclado.nextLine();

        System.out.println("Introduce el destino de la etapa:");
        String destino = teclado.nextLine();

        System.out.println("Introduce la distancia (km):");
        double distancia = teclado.nextDouble();
        teclado.nextLine();

        System.out.println("Introduce la fecha (YYYY-MM-DD):");
        String fecha = teclado.nextLine();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);
            try {
                // INSERT ETAPA
                String sqlEtapa =
                        "INSERT INTO ETAPA (NUMERO, ORIGEN, DESTINO, DISTANCIA_KM, FECHA) " +
                                "VALUES (?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))";
                PreparedStatement psEtapa = conn.prepareStatement(sqlEtapa);
                psEtapa.setInt(1, numeroEtapa);
                psEtapa.setString(2, origen);
                psEtapa.setString(3, destino);
                psEtapa.setDouble(4, distancia);
                psEtapa.setString(5, fecha);
                psEtapa.executeUpdate();
                // OBTENER CICLISTAS
                List<Integer> ciclistas = new ArrayList<>();
                String sqlCiclistas = "SELECT ID_CICLISTA FROM CICLISTA";
                ResultSet rs = conn.createStatement().executeQuery(sqlCiclistas);
                while (rs.next()) {
                    ciclistas.add(rs.getInt("ID_CICLISTA"));
                }
                int total = ciclistas.size();
                // GENERAR POSICIONES SIN REPETIR
                List<Integer> posiciones = new ArrayList<>();
                for (int i = 1; i <= total; i++) {
                    posiciones.add(i);
                }

                Collections.shuffle(posiciones);

                // INSERT PARTICIPACION
                String sqlPart =
                        "INSERT INTO PARTICIPACION (ID_CICLISTA, NUMERO_ETAPA, POSICION, PUNTOS) " +
                                "VALUES (?, ?, ?, ?)";

                PreparedStatement psPart = conn.prepareStatement(sqlPart);

                for (int i = 0; i < total; i++) {

                    int idCiclista = ciclistas.get(i);
                    int posicion = posiciones.get(i);

                    int puntos;

                    switch (posicion) {
                        case 1:
                            puntos = 100;
                        break;
                        case 2:
                            puntos = 90;
                        break;
                        case 3:
                            puntos = 80;
                        break;
                        case 4:
                            puntos = 70;
                        break;
                        case 5:
                            puntos = 60;
                        break;
                        default:
                            puntos = 0;
                    }

                    psPart.setInt(1, idCiclista);
                    psPart.setInt(2, numeroEtapa);
                    psPart.setInt(3, posicion);
                    psPart.setInt(4, puntos);

                    psPart.executeUpdate();
                }

                conn.commit();

                System.out.println("Etapa y participaciones insertadas correctamente.");

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error, transacción cancelada.");
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        teclado.close();
    }
}
