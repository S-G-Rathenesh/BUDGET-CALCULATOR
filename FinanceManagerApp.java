import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class Transaction {
    String type; // "Income" or "Expense"
    String category;
    double amount;
    String date; // Format: "YYYY-MM-DD"

    public Transaction(String type, String category, double amount, String date) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public String getMonthYear() {
        return date.substring(0, 7); // Extract YYYY-MM
    }

    public String getYear() {
        return date.substring(0, 4); // Extract YYYY
    }
}

public class FinanceManagerApp extends JFrame {
    private final List<Transaction> transactions; // Marked as final
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JTextArea txtSummary;
    private JTextArea txtYearlySummary;
    private JTextField txtYearInput;

    public FinanceManagerApp() {
        transactions = new ArrayList<>();
        setTitle("Finance Manager");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Transactions", createTransactionPanel());
        tabbedPane.addTab("Financial Summary", createSummaryPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table to display transactions
        tableModel = new DefaultTableModel(new Object[]{"Type", "Category", "Amount", "Date"}, 0);
        transactionTable = new JTable(tableModel);
        panel.add(new JScrollPane(transactionTable), BorderLayout.CENTER);

        // Input form
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        JLabel lblType = new JLabel("Type:");
        JComboBox<String> cmbType = new JComboBox<>(new String[]{"Income", "Expense"});
        JLabel lblCategory = new JLabel("Category:");
        JTextField txtCategory = new JTextField();
        JLabel lblAmount = new JLabel("Amount:");
        JTextField txtAmount = new JTextField();
        JLabel lblDate = new JLabel("Date (YYYY-MM-DD):");
        JTextField txtDate = new JTextField();
        JButton btnAdd = new JButton("Add Transaction");

        // Change button color
        btnAdd.setBackground(new Color(0, 150, 136)); // Teal color
        btnAdd.setForeground(Color.WHITE); // White text

        inputPanel.add(lblType);
        inputPanel.add(cmbType);
        inputPanel.add(lblCategory);
        inputPanel.add(txtCategory);
        inputPanel.add(lblAmount);
        inputPanel.add(txtAmount);
        inputPanel.add(lblDate);
        inputPanel.add(txtDate);
        inputPanel.add(new JLabel());
        inputPanel.add(btnAdd);

        panel.add(inputPanel, BorderLayout.NORTH);

        // Add Transaction button action
        btnAdd.addActionListener(e -> {
            try {
                String type = cmbType.getSelectedItem().toString();
                String category = txtCategory.getText().trim();
                double amount = Double.parseDouble(txtAmount.getText().trim());
                String date = txtDate.getText().trim();

                // Validate Date Format (Regex for YYYY-MM-DD)
                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD.");
                }

                // Add to transactions and table
                Transaction transaction = new Transaction(type, category, amount, date);
                transactions.add(transaction);
                tableModel.addRow(new Object[]{type, category, amount, date});

                // Clear inputs
                txtCategory.setText("");
                txtAmount.setText("");
                txtDate.setText("");

                updateSummary();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Amount must be a valid number.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please try again.");
            }
        });

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Monthly Summary
        txtSummary = new JTextArea();
        txtSummary.setFont(new Font("Arial", Font.PLAIN, 14));
        txtSummary.setEditable(false);
        txtSummary.setBorder(BorderFactory.createTitledBorder("Monthly Financial Summary"));

        // Yearly Summary
        JPanel yearlyPanel = new JPanel(new BorderLayout());
        txtYearlySummary = new JTextArea();
        txtYearlySummary.setFont(new Font("Arial", Font.PLAIN, 14));
        txtYearlySummary.setEditable(false);
        txtYearlySummary.setBorder(BorderFactory.createTitledBorder("Yearly Financial Summary"));

        // Input for specific year
        JPanel yearInputPanel = new JPanel(new FlowLayout());
        JLabel lblYearInput = new JLabel("Enter Year:");
        txtYearInput = new JTextField(10);
        JButton btnShowYearly = new JButton("Show Yearly Summary");

        // Change button color
        btnShowYearly.setBackground(new Color(33, 150, 243)); // Blue color
        btnShowYearly.setForeground(Color.WHITE); // White text

        yearInputPanel.add(lblYearInput);
        yearInputPanel.add(txtYearInput);
        yearInputPanel.add(btnShowYearly);

        yearlyPanel.add(new JScrollPane(txtYearlySummary), BorderLayout.CENTER);
        yearlyPanel.add(yearInputPanel, BorderLayout.SOUTH);

        btnShowYearly.addActionListener(e -> updateYearlySummary(txtYearInput.getText().trim()));

        panel.add(new JScrollPane(txtSummary), BorderLayout.CENTER);
        panel.add(yearlyPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateSummary() {
        // Group transactions by month-year
        Map<String, Double> incomeByMonth = new TreeMap<>();
        Map<String, Double> expenseByMonth = new TreeMap<>();

        for (Transaction t : transactions) {
            String monthYear = t.getMonthYear();
            if (t.type.equals("Income")) {
                incomeByMonth.put(monthYear, incomeByMonth.getOrDefault(monthYear, 0.0) + t.amount);
            } else {
                expenseByMonth.put(monthYear, expenseByMonth.getOrDefault(monthYear, 0.0) + t.amount);
            }
        }

        // Generate monthly summary
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("%-15s%-15s%-15s%-15s\n", "Month-Year", "Income", "Expense", "Net Savings"));
        summary.append("------------------------------------------------------------\n");

        for (String month : incomeByMonth.keySet()) {
            double income = incomeByMonth.getOrDefault(month, 0.0);
            double expense = expenseByMonth.getOrDefault(month, 0.0);
            double netSavings = income - expense;

            summary.append(String.format("%-15s%-15.2f%-15.2f%-15.2f\n", month, income, expense, netSavings));
        }

        txtSummary.setText(summary.toString());
    }

    private void updateYearlySummary(String year) {
        // Group transactions by year
        Map<String, Double> incomeByYear = new TreeMap<>();
        Map<String, Double> expenseByYear = new TreeMap<>();

        for (Transaction t : transactions) {
            String transactionYear = t.getYear();
            if (t.type.equals("Income")) {
                incomeByYear.put(transactionYear, incomeByYear.getOrDefault(transactionYear, 0.0) + t.amount);
            } else {
                expenseByYear.put(transactionYear, expenseByYear.getOrDefault(transactionYear, 0.0) + t.amount);
            }
        }

        // Generate yearly summary
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("%-10s%-15s%-15s%-15s\n", "Year", "Income", "Expense", "Net Savings"));
        summary.append("------------------------------------------------\n");

        for (String transactionYear : incomeByYear.keySet()) {
            if (year.isEmpty() || transactionYear.equals(year)) {
                double income = incomeByYear.getOrDefault(transactionYear, 0.0);
                double expense = expenseByYear.getOrDefault(transactionYear, 0.0);
                double netSavings = income - expense;

                summary.append(String.format("%-10s%-15.2f%-15.2f%-15.2f\n", transactionYear, income, expense, netSavings));
            }
        }

        txtYearlySummary.setText(summary.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FinanceManagerApp app = new FinanceManagerApp();
            app.setVisible(true);
        });
    }
}
