package com.reactive.preypredator.statistics;

/**
 * Calculates theoretical Lotka-Volterra population dynamics
 */
public class LotkaVolterraCalculator {
    private double alpha;   // Prey birth rate
    private double beta;    // Predation rate
    private double gamma;   // Predator death rate
    private double delta;   // Predator efficiency (prey -> predator conversion)

    private double dt = 0.1; // Time step for numerical integration

    public LotkaVolterraCalculator(double alpha, double beta, double gamma, double delta) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.delta = delta;
    }

    /**
     * Calculate LV populations using Runge-Kutta 4th order
     * @param x0 Initial prey population
     * @param y0 Initial predator population
     * @param ticks Number of ticks to simulate
     * @return Array of [preyAtTick, predatorAtTick] for each tick
     */
    public double[][] simulate(double x0, double y0, int ticks) {
        double[][] result = new double[ticks][2];

        double x = x0;
        double y = y0;

        for (int i = 0; i < ticks; i++) {
            result[i][0] = x;
            result[i][1] = y;

            // Runge-Kutta 4th order integration
            double[] k1 = derivatives(x, y);
            double[] k2 = derivatives(x + dt * k1[0] / 2, y + dt * k1[1] / 2);
            double[] k3 = derivatives(x + dt * k2[0] / 2, y + dt * k2[1] / 2);
            double[] k4 = derivatives(x + dt * k3[0], y + dt * k3[1]);

            x += dt * (k1[0] + 2 * k2[0] + 2 * k3[0] + k4[0]) / 6;
            y += dt * (k1[1] + 2 * k2[1] + 2 * k3[1] + k4[1]) / 6;

            // Prevent negative populations
            x = Math.max(0, x);
            y = Math.max(0, y);
        }

        return result;
    }

    /**
     * Calculate derivatives dx/dt and dy/dt
     */
    private double[] derivatives(double x, double y) {
        double dxdt = alpha * x - beta * x * y;
        double dydt = delta * x * y - gamma * y;
        return new double[]{dxdt, dydt};
    }

    /**
     * Estimate LV parameters from simulation data using least squares
     * @param data Array of [tick, preyCount, predatorCount]
     * @return Estimated [alpha, beta, gamma, delta]
     */
    public static double[] estimateParameters(double[][] data) {
        // Use only ticks where both populations > 0
        int n = 0;
        for (int i = 0; i < data.length - 1; i++) {
            if (data[i][1] > 0 && data[i][2] > 0 && data[i + 1][1] > 0 && data[i + 1][2] > 0) {
                n++;
            }
        }

        if (n < 4) {
            // Not enough data, return default values
            return new double[]{0.1, 0.001, 0.1, 0.0001};
        }

        // Build matrices for least squares
        double[][] A_prey = new double[n][2];
        double[] b_prey = new double[n];
        double[][] A_pred = new double[n][2];
        double[] b_pred = new double[n];

        int idx = 0;
        for (int i = 0; i < data.length - 1; i++) {
            double x = data[i][1];
            double y = data[i][2];
            double x_next = data[i + 1][1];
            double y_next = data[i + 1][2];

            if (x > 0 && y > 0 && x_next > 0 && y_next > 0) {
                // dx/dt = alpha*x - beta*x*y
                A_prey[idx][0] = x;
                A_prey[idx][1] = -x * y;
                b_prey[idx] = x_next - x;

                // dy/dt = delta*x*y - gamma*y
                A_pred[idx][0] = x * y;
                A_pred[idx][1] = -y;
                b_pred[idx] = y_next - y;

                idx++;
            }
        }

        // Solve using simple normal equations
        double[] params_prey = solveLeastSquares(A_prey, b_prey, n);
        double[] params_pred = solveLeastSquares(A_pred, b_pred, n);

        return new double[]{
                Math.max(0.001, params_prey[0]),  // alpha
                Math.max(0.00001, params_prey[1]), // beta
                Math.max(0.001, params_pred[1]),   // gamma
                Math.max(0.00001, params_pred[0])  // delta
        };
    }

    private static double[] solveLeastSquares(double[][] A, double[] b, int n) {
        // Normal equations: A^T * A * x = A^T * b
        double a11 = 0, a12 = 0, a22 = 0, b1 = 0, b2 = 0;

        for (int i = 0; i < n; i++) {
            a11 += A[i][0] * A[i][0];
            a12 += A[i][0] * A[i][1];
            a22 += A[i][1] * A[i][1];
            b1 += A[i][0] * b[i];
            b2 += A[i][1] * b[i];
        }

        double det = a11 * a22 - a12 * a12;
        if (Math.abs(det) < 1e-10) {
            return new double[]{0.1, 0.001}; // Default
        }

        double x1 = (a22 * b1 - a12 * b2) / det;
        double x2 = (a11 * b2 - a12 * b1) / det;

        return new double[]{x1, x2};
    }
}
