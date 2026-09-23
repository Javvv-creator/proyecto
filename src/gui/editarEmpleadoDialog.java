package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import main.Crud.crud;

public class editarEmpleadoDialog extends JDialog {

    private final int idUsuario;
    private boolean guardado = false;

    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtApellido = new JTextField(20);
    private final JTextField txtCodigo = new JTextField(20);
    private final JComboBox<String> cbRol = new JComboBox<>(new String[]{"Administrador", "Cajero"});
    private final JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Activo", "Inactivo"});

    public editarEmpleadoDialog(JFrame parent, int idUsuario) {
        super(parent, "Editar empleado", true);
        this.idUsuario = idUsuario;

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] etiquetas = {"Nombre:", "Apellido:", "Código:", "Rol:", "Estado:"};
        JComponent[] campos = {txtNombre, txtApellido, txtCodigo, cbRol, cbEstado};
        for (int i = 0; i < etiquetas.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            form.add(new JLabel(etiquetas[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            form.add(campos[i], gbc);
        }

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        cargarDatos();
        pack();
        setLocationRelativeTo(parent);
    }

    private void cargarDatos() {
        Object[] u = crud.obtenerUsuario(idUsuario);
        if (u == null) {
            JOptionPane.showMessageDialog(this, "No se encontró el usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        txtNombre.setText((String) u[0]);
        txtApellido.setText((String) u[1]);
        txtCodigo.setText((String) u[2]);
        cbRol.setSelectedItem("ADMINISTRADOR".equalsIgnoreCase((String) u[3]) ? "Administrador" : "Cajero");
        cbEstado.setSelectedItem(((Integer) u[4]) == 1 ? "Activo" : "Inactivo");
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String codigo = txtCodigo.getText().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rol = (String) cbRol.getSelectedItem();
        int estado = "Activo".equals(cbEstado.getSelectedItem()) ? 1 : 0;

        boolean ok = crud.editarUsuario(idUsuario, nombre, apellido, codigo, rol, estado);
        if (ok) {
            guardado = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar (¿código duplicado?).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isGuardado() {
        return guardado;
    }
}