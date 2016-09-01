package org.somox.ejbmox.inspectit2pcm.parametrization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.junit.Assert;
import org.junit.Test;
import org.palladiosimulator.pcm.core.PCMRandomVariable;

import de.uka.ipd.sdq.probfunction.math.IProbabilityDensityFunction;
import de.uka.ipd.sdq.probfunction.math.ManagedPDF;
import de.uka.ipd.sdq.probfunction.math.exception.StringNotPDFException;

public class TestAggregationStrategies {

    private static final double MEAN_COMPARISON_DELTA = 0.000001;

    private static final double ACCEPTABLE_ERROR = 0.02; // fraction between 0...1
    private static final int DISTRIBUTION_BIN_COUNT = 10;

    @Test
    public void testMeanAggregation() {
        AggregationStrategy strategy = new MeanAggregationStrategy();
        PCMRandomVariable rv = strategy.aggregate(VALUES_LIST);
        double actualMean = Double.parseDouble(rv.getSpecification());
        Assert.assertEquals(5.0644507210917, actualMean, MEAN_COMPARISON_DELTA);
    }

    @Test
    public void testDistributionAggregation_Empirical() throws StringNotPDFException, RecognitionException {
        // calculate expected statistics
        DescriptiveStatistics expectedStatistics = statistics(VALUES_LIST);

        // create PDF using aggregation strategy
        IProbabilityDensityFunction pdf = createDistributionOf(VALUES_LIST);

        // draw samples from PDF and calculate sample statistics
        DescriptiveStatistics sampleStatistics = statistics(drawSamples(pdf, 1_000_000));

        double sampleMean = sampleStatistics.getMean();
        double expectedMean = expectedStatistics.getMean();
        double delta = calculateTotalAcceptableError(expectedMean);
        Assert.assertEquals(expectedMean, sampleMean, delta);
    }

    private double calculateTotalAcceptableError(double expectedMean) {
        return expectedMean * ACCEPTABLE_ERROR;
    }

    @Test
    public void testDistributionAggregation_normal() throws StringNotPDFException, RecognitionException {
        testDistributionAggregation_RealDistribution(new NormalDistribution(5.0, 2.0));
    }
    
    @Test
    public void testDistributionAggregation_exponential() throws StringNotPDFException, RecognitionException {
        testDistributionAggregation_RealDistribution(new ExponentialDistribution(100.0));
    }

    @Test
    public void testDistributionAggregation_uniform() throws StringNotPDFException, RecognitionException {
        testDistributionAggregation_RealDistribution(new UniformRealDistribution(0.0, 10.0));
    }

    @Test
    public void testDistributionAggregation_uniform_narrow() throws StringNotPDFException, RecognitionException {
        testDistributionAggregation_RealDistribution(new UniformRealDistribution(0.0, 1.0));
    }

    @Test
    public void testDistributionAggregation_uniform_wide() throws StringNotPDFException, RecognitionException {
        testDistributionAggregation_RealDistribution(new UniformRealDistribution(0.0, 100.0));
    }

    private void testDistributionAggregation_RealDistribution(AbstractRealDistribution distribution)
            throws RecognitionException {
        // calculate expected statistics
        Collection<Double> values = drawSamples(distribution, 1_000);
        DescriptiveStatistics expectedStatistics = statistics(drawSamples(distribution, 1_000_000));

        // create PDF using aggregation strategy
        IProbabilityDensityFunction pdf = createDistributionOf(values);

        // draw samples from PDF and calculate sample statistics
        DescriptiveStatistics sampleStatistics = statistics(drawSamples(pdf, 1_000_000));

        double sampleMean = sampleStatistics.getMean();
        double expectedMean = expectedStatistics.getMean();
        double delta = calculateTotalAcceptableError(expectedMean);
        Assert.assertEquals(expectedMean, sampleMean, delta);
    }

    private DescriptiveStatistics statistics(Collection<Double> values) {
        DescriptiveStatistics statistics = new DescriptiveStatistics();
        for (Double v : values) {
            statistics.addValue(v);
        }
        return statistics;
    }

    private List<Double> drawSamples(AbstractRealDistribution pdf, int count) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double sample = pdf.sample();
            values.add(sample);
        }
        return values;
    }

    private List<Double> drawSamples(IProbabilityDensityFunction pdf, int count) {
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double sample = pdf.drawSample();
            values.add(sample);
        }
        return values;
    }

    private IProbabilityDensityFunction createDistributionOf(Collection<Double> values) throws RecognitionException {
        AggregationStrategy strategy = new DistributionAggregationStrategy(DISTRIBUTION_BIN_COUNT);
        PCMRandomVariable rv = strategy.aggregate(values);
        String stoEx = rv.getSpecification();

        // create PDF from StoEx
        IProbabilityDensityFunction pdf = ManagedPDF.createFromString(stoEx).getPdfTimeDomain();
        return pdf;
    }

    private static final Double[] VALUES = new Double[] { 6.621472000144422, 5.491840000031516, 6.118447999935597,
            5.906688000075519, 5.614895999897271, 5.637039999943227, 5.540847999975085, 5.480911999940872,
            5.403231999836862, 5.523712000111118, 12.112383999861777, 6.746143999975175, 5.125455999979749,
            5.3888640000950545, 5.027680000057444, 5.616432000184432, 5.425695999991149, 5.104224000126123,
            5.2636480000801384, 5.185231999959797, 5.498175999848172, 5.34689599997364, 5.34155200002715,
            7.242687999969348, 5.253008000086993, 5.230832000030205, 5.308480000123382, 5.724544000113383,
            5.019167999969795, 5.4171839999035, 5.172687999904156, 5.611039999872446, 5.435728000011295,
            4.8727520001120865, 5.263439999893308, 4.836320000002161, 4.86539199994877, 4.808815999887884,
            4.906639999942854, 4.98406400019303, 5.171071999939159, 6.255024000070989, 4.969648000085726,
            4.901247999863699, 7.072367999935523, 5.470288000069559, 11.71372799994424, 5.671007999917492,
            4.912336000008509, 5.060655999928713, 4.881135999923572, 5.449391999980435, 5.855151999974623,
            5.347056000027806, 7.186607999959961, 4.937168000033125, 4.81843200000003, 4.787280000047758,
            4.851391999982297, 5.155184000032023, 5.296319999964908, 5.293951999861747, 4.99660800001584,
            5.284864000044763, 4.915456000017002, 5.587535999948159, 4.817888000048697, 4.834496000083163,
            5.71516799996607, 4.8092320000287145, 4.967759999912232, 5.584016000153497, 4.816223999951035,
            4.9404160000849515, 4.904991999967024, 5.07059200014919, 4.844400000059977, 6.802799999946728,
            5.836991999996826, 4.73756800009869, 4.724144000094384, 4.807695999974385, 4.758495999965817,
            4.9517119999509305, 5.038592000026256, 4.776031999848783, 4.982480000006035, 4.786112000001594,
            4.743759999983013, 4.9517119999509305, 6.542080000042915, 5.344191999873146, 4.997200000099838,
            5.142943999962881, 4.989263999974355, 4.661855999846011, 5.235535999992862, 4.768880000105128,
            5.01356799993664, 6.034592000069097, 4.582607999909669, 6.499952000100166, 5.039199999999255,
            5.46249600010924, 5.023039999883622, 4.958623999962583, 5.288288000039756, 19.891440000152215,
            5.576831999933347, 4.799583999905735, 4.860080000013113, 4.736575999995694, 4.974256000015885,
            4.588592000072822, 14.66256000008434, 4.661968000000343, 4.633855999913067, 4.535519999917597,
            4.624368000077084, 4.891888000071049, 4.822367999935523, 4.9881279999390244, 4.412832000060007,
            4.691904000006616, 5.61980800004676, 4.612559999804944, 5.664319999981672, 4.867375999921933,
            4.904768000124022, 4.882784000132233, 4.587007999885827, 4.620608000084758, 4.556496000150219,
            6.458544000051916, 4.5164159999694675, 4.603232000023127, 4.727408000035211, 4.778656000038609,
            4.489776000147685, 7.056080000009388, 4.908832000102848, 4.505200000014156, 4.7346080001443624,
            4.520895999856293, 5.364335999824107, 4.600016000214964, 4.455360000021756, 4.502944000065327,
            4.529375999933109, 4.49198400019668, 6.333968000020832, 4.634751999983564, 4.463791999965906,
            4.627024000044912, 4.812031999928877, 4.474160000216216, 4.537679999833927, 4.51993599999696,
            4.676911999937147, 4.539120000088587, 4.490464000031352, 5.005968000041321, 5.130511999828741,
            4.6026720001827925, 4.448287999955937, 4.472624000161886, 5.3412319999188185, 4.863056000089273,
            4.472544000018388, 5.229472000151873, 8.118640000000596, 11.249312000116333, 4.424432000145316,
            6.289583999896422, 4.5775039999280125, 5.450703999958932, 4.510896000079811, 4.399104000069201,
            4.281072000041604, 4.731472000014037, 4.593392000067979, 4.310064000077546, 4.418800000101328,
            4.136639999924228, 4.343007999937981, 5.051519999979064, 4.264960000058636, 4.2620480000041425,
            4.718751999782398, 4.291504000080749, 4.275872000027448, 4.322064000181854, 4.533968000207096,
            4.380144000053406, 4.378575999988243, 4.519103999948129, 4.4666239998769015, 4.338319999864325,
            4.725824000081047, 4.27291200007312, 4.966848000185564, 4.339504000032321, 4.373055999865755,
            4.6913920000661165, 4.245584000134841, 4.430191999999806, 4.253711999859661, 4.257471999851987,
            4.5803360000718385, 5.5474399998784065, 5.503935999935493, 5.420143999857828, 4.97328000003472,
            5.507423999952152, 4.408496000105515, 4.363103999989107, 6.16399999987334, 4.607311999890953,
            4.424608000088483, 4.253503999905661, 6.221279999939725, 4.734127999981865, 4.289568000007421,
            4.3068480000365525, 5.365791999967769, 8.10377600020729, 4.731472000014037, 4.577231999952346,
            4.434895999962464, 4.6458400001283735, 4.333295999793336, 4.290255999891087, 4.692911999998614,
            4.578351999865845, 4.414880000054836, 4.350175999803469, 4.315551999956369, 5.094768000068143,
            4.939695999957621, 4.437072000000626, 10.034991999855265, 4.234528000000864, 4.224479999858886,
            4.250175999943167, 4.25513599999249, 4.592367999954149, 4.583871999988332, 4.182528000092134,
            5.605568000115454, 4.868672000011429, 4.452992000151426, 4.216383999912068, 4.247808000072837,
            4.57161600003019, 4.335008000023663, 4.1336799999699, 4.5268000001087785, 4.467151999939233,
            4.278272000141442, 4.287263999925926, 4.117375999921933, 4.852384000085294, 4.086879999842495,
            4.279711999930441, 4.711072000209242, 4.106304000131786, 4.41985599999316, 4.046912000048906,
            4.4246079998556525, 4.121615999843925, 4.088959999848157, 4.370912000071257, 5.0084800000768155,
            4.171616000123322, 4.956672000000253, 4.169632000150159, 4.08713599992916, 4.1175519998651,
            4.678512000013143, 4.0596320000477135, 4.161535999970511, 4.058511999901384, 3.9409119999036193,
            4.116512000095099, 4.36323200003244, 4.113904000027105, 4.598015999887139, 4.017855999991298,
            4.1264959999825805, 3.983312000054866, 4.1523680000100285, 4.199248000048101, 4.260080000152811,
            4.13433600007556 };

    private static final List<Double> VALUES_LIST = Arrays.asList(VALUES);

}
