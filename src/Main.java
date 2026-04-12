import java.sql.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String user = "USUARIO";
        String password = "ribera";

        // 1. Pedir datos correctamente
        System.out.println("Introduce el numero de la etapa:");
        int numeroetapa = teclado.nextInt();
        teclado.nextLine(); // 🔥 limpiar buffer

        System.out.println("Introduce el origen de la etapa:");
        String origen = teclado.nextLine();

        System.out.println("Introduce el destino de la etapa:");
        String destino = teclado.nextLine();

        System.out.println("Introduce la distancia (km) de la etapa:");
        double distancia = teclado.nextDouble();
        teclado.nextLine(); //  limpiar buffer

        System.out.println("Introduce la fecha de la etapa (YYYY-MM-DD):");
        String fecha = teclado.nextLine();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // 2. Transacción
            conn.setAutoCommit(false);
            try {
                // 3. Insertar etapa (Oracle)
                String sqlEtapa = "INSERT INTO ETAPA (numero_etapa, origen, destino, distancia, fecha) " +
                        "VALUES (?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'))";
                PreparedStatement ps = conn.prepareStatement(sqlEtapa);

                ps.setInt(1, numeroetapa);
                ps.setString(2, origen);
                ps.setString(3, destino);
                ps.setDouble(4, distancia);
                ps.setString(5, fecha);

                ps.executeUpdate();

                // 4. Obtener ciclistas
                String sqlCiclistas = "SELECT id_ciclista FROM CICLISTA";
                ResultSet rs = conn.createStatement().executeQuery(sqlCiclistas);

                List<Integer> ciclista = new ArrayList<>();

                while (rs.next()) {
                    ciclista.add(rs.getInt("id_ciclista"));
                }

                int totalCiclista = ciclista.size();

                // Generar posiciones sin repetir
                List<Integer> posicion = new ArrayList<>();
                Random rand = new Random();

                while (posicion.size() < totalCiclista) {
                    int p = rand.nextInt(totalCiclista) + 1;
                    if (!posicion.contains(p)) {
                        posicion.add(p);
                    }
                }

                // Insertar participaciones
                String sqlParticipacion = "INSERT INTO PARTICIPACION VALUES (?, ?, ?, ?)";
                PreparedStatement psParticipacion = conn.prepareStatement(sqlParticipacion);

                for (int i = 0; i < totalCiclista; i++) {

                    int pos = posicion.get(i);
                    int puntos;

                    switch (pos) {
                        case 1: puntos = 100; break;
                        case 2: puntos = 90; break;
                        case 3: puntos = 80; break;
                        case 4: puntos = 70; break;
                        case 5: puntos = 60; break;
                        default: puntos = 0;
                    }

                    psParticipacion.setInt(1, numeroetapa);
                    psParticipacion.setInt(2, ciclista.get(i));
                    psParticipacion.setInt(3, pos);
                    psParticipacion.setInt(4, puntos);

                    psParticipacion.executeUpdate();
                }

                // Commit
                conn.commit();

                System.out.println("\nEtapa insertada correctamente.");
                System.out.println("Número de etapa: " + numeroetapa);
                System.out.println("Total ciclistas: " + totalCiclista);
                System.out.println("Fecha: " + fecha);

            } catch (SQLException e) {

                // 5. Rollback
                conn.rollback();

                System.out.println("Etapa cancelada por error. No se guardaron los datos.");
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        teclado.close();
    }
}