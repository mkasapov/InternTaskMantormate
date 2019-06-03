import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import objectmodels.Employee;
import objectmodels.ReportDefinition;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Application {
    public static void main(String[] args) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> jsonPaths = new ArrayList<>();
        String line;
        for (int i = 0; i <= 1; i++) {
            line = reader.readLine();
            jsonPaths.add(line);
        }

        String employeeJsonPath = jsonPaths.get(0);
        String reportDefinitionJsonPath = jsonPaths.get(1);

        ObjectMapper mapper = new ObjectMapper();
        List<Employee> employees = mapper.readValue(new File(employeeJsonPath), new TypeReference<List<Employee>>() {
        });
        final ReportDefinition reportDefinition = mapper
                .readValue(new File(reportDefinitionJsonPath), new TypeReference<ReportDefinition>() {
                });

        List<Employee> employeesWithValidSalesPeriod = getEmployeesWithValidSalesPeriod(employees, reportDefinition);

        List<Double> filteredScores = getValidScores(reportDefinition, employeesWithValidSalesPeriod);

        writeCsvResultFile(employeesWithValidSalesPeriod, filteredScores);


    }

    private static List<Employee> getEmployeesWithValidSalesPeriod(List<Employee> employeeObjects, ReportDefinition reportDefinition) {
        return employeeObjects.stream()
                .filter(emp -> emp.getSalesPeriod() <= reportDefinition.getPeriodLimit())
                .collect(Collectors.toList());
    }

    private static void writeCsvResultFile(List<Employee> employeesWithGoodSalesPeriod, List<Double> filteredScores) throws IOException {
        FileWriter csvWriter = new FileWriter("result.csv");
        csvWriter.append("Name");
        csvWriter.append(",");
        csvWriter.append("Score");
        csvWriter.append("\n");

        for (Employee employee : employeesWithGoodSalesPeriod) {
            if (filteredScores.contains(employee.getScore())) {
                csvWriter.append(employee.getName() + ", ");
                csvWriter.append(employee.getScore() + "");
                csvWriter.append("\n");
            }
        }
        csvWriter.flush();
        csvWriter.close();
    }

    private static List<Double> getValidScores(ReportDefinition reportDefinition, List<Employee> employeesWithGoodSalesPeriod) {
        double score = 0;
        List<Double> scores = new ArrayList<>();
        for (Employee employee : employeesWithGoodSalesPeriod) {
            if (reportDefinition.isUseExperienceMultiplier()) {
                score = (employee.getTotalSales() / employee.getSalesPeriod()) * employee.getExperienceMultiplier();
            } else {
                score = employee.getTotalSales() / employee.getSalesPeriod();
            }
            scores.add(score);
            employee.setScore(score);
        }
        Collections.sort(scores);
        int numberOfTopScores = scores.size() * reportDefinition.getTopPerformersThreshold() / 100;

        return scores.stream().limit(numberOfTopScores).collect(Collectors.toList());
    }
}
