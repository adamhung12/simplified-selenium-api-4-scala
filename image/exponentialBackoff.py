import matplotlib.pyplot as plt
import pandas as pd

def exponentialBackoff(x):
   const = 0.5
   if(x==1):
       return const
   elif (x > 1 and x < 20):
       return 0.2 * x * x + const
   else:
       return 80 + const



xy = [(x, exponentialBackoff(x))for x in range(1, 30)]
xy = pd.DataFrame(xy, columns=(["x","y"]))
plt.plot(xy["x"],xy["y"])