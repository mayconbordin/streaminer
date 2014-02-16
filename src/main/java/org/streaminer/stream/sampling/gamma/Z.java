/*
 * Copyright (c)  2010 Ghais Issa and others. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.streaminer.stream.sampling.gamma;

import java.util.Random;

/**
 * Implementation of the Z algorithm.
 * @author Ghais Issa
 */
final public class Z implements GammaFunction {
    private static Random generator = new Random();

    private final int n;

    private double w;

    public Z(int n) {
        super();
        this.n = n;
        this.w = Math.exp(-Math.log(generator.nextDouble()) / n);
    }

    @Override
    public long apply(long t) {
        double term = t - this.n + 1;
        double u;
        double x;
        long gamma;
        while (true) {
            //generate u and x
            u = generator.nextDouble();
            x = t * (this.w - 1.0);
            gamma = (long) x;
            //test if u <= h(gamma)/cg(x)
            double lhs = Math.exp(Math.log(((u * Math.pow(((t + 1) / term), 2)) * (term + gamma)) / (t + x)) / this.n);
            double rhs = (((t + x) / (term + gamma)) * term) / t;
            if (lhs < rhs) {
                this.w = rhs / lhs;
                break;
            }
            //test if u <= f(gamma)/cg(x)
            double y = (((u * (t + 1)) / term) * (t + gamma + 1)) / (t + x);
            double denom;
            double number_lim;
            if (this.n < gamma) {
                denom = t;
                number_lim = term + gamma;
            } else {
                denom = t - this.n + gamma;
                number_lim = t + 1;
            }

            for (long number = t + gamma; number >= number_lim; number--) {
                y = (y * number) / denom;
                denom = denom - 1;
            }
            this.w = Math.exp(-Math.log(generator.nextDouble()) / this.n);
            if (Math.exp(Math.log(y) / this.n) <= (t + x) / t) {
                break;
            }
        }
        return gamma;

    }
}