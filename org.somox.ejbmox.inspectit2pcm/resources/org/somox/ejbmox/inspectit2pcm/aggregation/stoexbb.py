import numpy as np
from astroML.plotting import hist
import matplotlib.pyplot as plt

def main():
    import sys
    try:
        csv_path = sys.argv[1]
    except:
        print("usage: " + sys.argv[0] + " <path_to_csv_file>")
        exit()

    df = np.genfromtxt(csv_path)

    h = hist(df, bins='blocks', histtype='step', normed=False, label='standard histogram')
    frequencies = [0] + h[0].tolist()
    boundaries = h[1].tolist()
    stoex = "DoublePDF["
    for f, b in zip(frequencies, boundaries):
        prob = f/sum(frequencies)
        stoex += "(" + str(b) + ";" + str(prob) + ")"
    stoex += "]"
    print(stoex)
    #plt.show()

if __name__ == "__main__":
    main()