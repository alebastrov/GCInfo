 ## This small app lets you embed information with short memory & GC status as well as graph of GC usage. 
 It returns HTML like reflects actual status of memory and GC (see example below).
 
![Ashampoo_Snap_2019 07 31_17h50m09s_040_](https://user-images.githubusercontent.com/6348292/62222264-be089300-b3bb-11e9-9c81-7e809b67f41e.png)

Just put compiled files into /WEB-INF/classes and use
~~~
new GCInfo2HtmlPrinter().getHtml();
~~~

What we can see here?
Long horizontal bar reveals current memory usage - left (red) side - memory is in use, middle (white) is available memory without reallocation and GC, right (green) - reflects total amount of memory

For instance on image we can see 984 MiB memory total (-Xmx1g), Free memory 579 MiB, Used memory 404 MiB, red part of horizontal bar - 404 of total bar 984, white part is 579 of 984.

There are bars on the image, each bar reflects GC job. The bar's height reflects how much memory GC freed, the bar's width reflects GC time taken for collecting, and color of the bar shows how % time GC took. The black bars on image reflects Stop-The-World or GC state near to it.


#### Note: first run will show an empty image, because it will start collecting GC events right that moment. You may like to start collecting with starting your application, in that case please put initializer nelow in your start-up class
~~~
GCInfoCollector.getGCInfoCollector(TimeUnit.SECONDS.toMillis(10));
~~~
10 seconds is the update time period, so if you take image often than 10 seconds, it may look stale.

Have fun!
