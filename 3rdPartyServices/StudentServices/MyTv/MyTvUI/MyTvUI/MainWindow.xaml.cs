﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using Coding4Fun.Kinect.Wpf;
using Coding4Fun.Kinect;
using Coding4Fun.Kinect.Wpf.Controls;
using System.Reflection;
using System.IO;
using System.Threading;
using System.Runtime.InteropServices;
using System.Net;
using Microsoft.Kinect;

namespace MyTvUI
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        #region variables
        //user variables
        String userID;

        //socket variables
        SocketClient socketClient;
        SocketServer socketServer;

        //window variables
        bool closing = false;

        //variables used to detect hand over hover button area
        private static double _topBoundary;
        private static double _bottomBoundary;
        private static double _leftBoundary;
        private static double _rightBoundary;
        private static double _itemLeft;
        private static double _itemTop;

        //skeleton tracking variables
        const int skeletonCount = 6;
        Skeleton[] allSkeletons = new Skeleton[skeletonCount];

        //Volume variables
        //int masterVolume;
        //int muteState;
        #endregion variables

        #region window
        public MainWindow()
        {
            //Initialise assemblies
            AppDomain.CurrentDomain.AssemblyResolve += new ResolveEventHandler(ResolveAssembly);

            //initialise GUI
            Console.WriteLine("Initialising GUI");
            InitializeComponent();
            channel1HoverRegion.Click += new RoutedEventHandler(channel1HoverRegion_Click);
            channel2HoverRegion.Click += new RoutedEventHandler(channel2HoverRegion_Click);
            channel3HoverRegion.Click += new RoutedEventHandler(channel3HoverRegion_Click);
            channel4HoverRegion.Click += new RoutedEventHandler(channel4HoverRegion_Click);
            offHoverRegion.Click += new RoutedEventHandler(offHoverRegion_Click);
            volumeUpHoverRegion.Click += new RoutedEventHandler(volumeUpHoverRegion_Click);
            volumeDownHoverRegion.Click += new RoutedEventHandler(volumeDownHoverRegion_Click);
            exitHoverRegion.Click += new RoutedEventHandler(exitHoverRegion_Click);
            tvBrowser.Navigated += new NavigatedEventHandler(tvBrowser_Navigated);

            //initialise volume

            //initialise socket server to listen for service client connections
            Console.WriteLine("Initialising SocketServer");
            //initialiseSocketServer();
            
            //Connect to service client - on user cloud node
            Console.WriteLine("Initialising SocketClient");
            //initialiseSocketClient();
            
        }

        //when window loaded
        private void Window_Loaded(object sender, RoutedEventArgs e)
        {
            //add sensor change listener to the kinect sensor chooser
            kinectSensorChooser1.KinectSensorChanged += new DependencyPropertyChangedEventHandler(kinectSensorChooser1_KinectSensorChanged);
        }

        //close window
        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            closing = true;
            StopKinect(kinectSensorChooser1.Kinect);
        }
        #endregion window

        #region sockets
        private void initialiseSocketServer()
        {
            socketServer = new SocketServer();
            Thread serverThread = new Thread(new ThreadStart(socketServer.run));
            serverThread.Start();
        }

        private void initialiseSocketClient()
        {
            socketClient = new SocketClient();
            if (socketClient.getSessionParameters())
            {
                userID = socketClient.getUserID();
                Console.WriteLine("Received user identity: " + userID);

                if (socketClient.connectToServiceClient())
                {
                    Console.WriteLine("Connected to service client on user cloud node!");
                    //send handshake message with GUI IP address
                    IPAddress[] localIPAddresses = Dns.GetHostAddresses(Dns.GetHostName());
                    String myIP = localIPAddresses[0].ToString();
                    if (socketClient.sendMessage(
                        "GUI_STARTED\n" +
                        myIP))
                    {
                        Console.WriteLine("Handshake complete");
                    }
                    else
                    {
                        Console.WriteLine("Handshake failed");
                    }
                }
                else
                {
                    Console.WriteLine("Could not connect to service client on user cloud node");
                }
            }
            else
            {
                Console.WriteLine("Error - could not get session parameters - userID and endpoint");
            }
        }
        #endregion sockets

        #region kinect
        //listener for kinect sensor change events
        void kinectSensorChooser1_KinectSensorChanged(object sender, DependencyPropertyChangedEventArgs e)
        {
            KinectSensor oldSensor = (KinectSensor)e.OldValue;
            StopKinect(oldSensor);

            KinectSensor newSensor = (KinectSensor)e.NewValue;
            newSensor.ColorStream.Enable(ColorImageFormat.RgbResolution640x480Fps30);
            newSensor.DepthStream.Enable(DepthImageFormat.Resolution640x480Fps30);
            newSensor.SkeletonStream.Enable();
            newSensor.AllFramesReady += new EventHandler<AllFramesReadyEventArgs>(_sensor_AllFramesReady);
            try
            {
                newSensor.Start();
            }
            catch (System.IO.IOException)
            {
                kinectSensorChooser1.AppConflictOccurred();
            }
        }

        //stop connect sensor
        void StopKinect(KinectSensor sensor)
        {
            if (sensor != null)
            {
                sensor.Stop();
                sensor.AudioSource.Stop();
            }
        }

        void _sensor_AllFramesReady(object sender, AllFramesReadyEventArgs e)
        {
            if (closing)
            {
                return;
            }

            Skeleton first = GetFirstSkeleton(e);

            if (first == null)
            {
                return;
            }

            GetCameraPoint(first, e);

            //ScalePosition(leftEllipse, first.Joints[JointType.HandLeft]);
            ScalePosition(rightEllipse, first.Joints[JointType.HandRight]);

            CheckHoverButton(channel1HoverRegion, rightEllipse);
            CheckHoverButton(channel2HoverRegion, rightEllipse);
            CheckHoverButton(channel3HoverRegion, rightEllipse);
            CheckHoverButton(channel4HoverRegion, rightEllipse);
            CheckHoverButton(offHoverRegion, rightEllipse);
        }
        #endregion kinect


        #region skeleton
        private Skeleton GetFirstSkeleton(AllFramesReadyEventArgs e)
        {
            using (SkeletonFrame skeletonFrameData = e.OpenSkeletonFrame())
            {
                if (skeletonFrameData == null)
                {
                    return null;
                }

                skeletonFrameData.CopySkeletonDataTo(allSkeletons);

                Skeleton first = (from s in allSkeletons
                                  where s.TrackingState == SkeletonTrackingState.Tracked
                                  select s).FirstOrDefault();

                return first;
            }
        }

        private void GetCameraPoint(Skeleton first, AllFramesReadyEventArgs e)
        {
            using (DepthImageFrame depth = e.OpenDepthImageFrame())
            {
                if (depth == null ||
                    kinectSensorChooser1.Kinect == null)
                {
                    return;
                }

                //get right hand depth from skeleton
                DepthImagePoint rightDepthPoint =
                    depth.MapFromSkeletonPoint(first.Joints[JointType.HandRight].Position);

                //get right hand colour from depth
                ColorImagePoint rightColorPoint =
                    depth.MapToColorImagePoint(rightDepthPoint.X, rightDepthPoint.Y,
                    ColorImageFormat.RgbResolution640x480Fps30);

                CameraPosition(rightEllipse, rightColorPoint);
            }
        }

        private void ScalePosition(FrameworkElement element, Joint joint)
        {
            Joint scaledJoint = joint.ScaleTo(1000, 600, .3f, .3f);

            Canvas.SetLeft(element, scaledJoint.Position.X);
            Canvas.SetTop(element, scaledJoint.Position.Y);
        }

        private void CameraPosition(FrameworkElement element, ColorImagePoint point)
        {
            Canvas.SetLeft(element, point.X - element.Width / 2);
            Canvas.SetTop(element, point.Y - element.Height / 2);
        }
        #endregion skeleton

        #region hoverbutton
        //check to see if right hand ellipse is in hover button region
        private void CheckHoverButton(HoverButton hoverButtonRegion, Ellipse ellipse)
        {
            if (IsPointInRegion(hoverButtonRegion, ellipse))
            {
                hoverButtonRegion.Hovering();
            }
            else
            {
                hoverButtonRegion.Release();
            }
        }

        private bool IsPointInRegion(FrameworkElement region, FrameworkElement point)
        {
            FindValues(region, point);

            if (_itemTop < _topBoundary || _bottomBoundary < _itemTop)
            {
                //Midpoint of target is outside of top or bottom
                return false;
            }

            if (_itemLeft < _leftBoundary || _rightBoundary < _itemLeft)
            {
                //Midpoint of target is outside of left or right
                return false;
            }

            return true;
        }

        private static void FindValues(FrameworkElement region, FrameworkElement point)
        {
            var containerTopLeft = region.PointToScreen(new Point());
            var itemTopLeft = point.PointToScreen(new Point());

            _topBoundary = containerTopLeft.Y;
            _bottomBoundary = _topBoundary + region.ActualHeight;
            _leftBoundary = containerTopLeft.X;
            _rightBoundary = _leftBoundary + region.ActualWidth;

            //use midpoint of item (width or height divided by 2)
            _itemLeft = itemTopLeft.X + (point.ActualWidth / 2);
            _itemTop = itemTopLeft.Y + (point.ActualHeight / 2);
        }

        //listener for channel1 hover button click events
        void channel1HoverRegion_Click(object sender, RoutedEventArgs e)
        {
            tvBrowser.Navigate("http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/channels/channel1.html");
        }

        //listener for channel2 hover button click events
        void channel2HoverRegion_Click(object sender, RoutedEventArgs e)
        {
            tvBrowser.Navigate("http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/channels/channel2.html");
        }

        //listener for channel3 hover button click events
        void channel3HoverRegion_Click(object sender, RoutedEventArgs e)
        {
            tvBrowser.Navigate("http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/channels/channel3.html");
        }

        void channel4HoverRegion_Click(object sender, RoutedEventArgs e)
        {
            tvBrowser.Navigate("http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/channels/channel4.html");
        }

        void offHoverRegion_Click(object sender, RoutedEventArgs e)
        {
            tvBrowser.Navigate("http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/channels/splashScreen.html");
        }

        void volumeDownHoverRegion_Click(object sender, RoutedEventArgs e)
        {
            throw new NotImplementedException();
        }

        void volumeUpHoverRegion_Click(object sender, RoutedEventArgs e)
        {
            throw new NotImplementedException();
        }

        void exitHoverRegion_Click(object sender, RoutedEventArgs e)
        {
            throw new NotImplementedException();
        }

        #endregion hoverbutton


        #region additional
        private void tvBrowser_WindowLoaded(object sender, RoutedEventArgs e)
        {
            tvBrowser.Navigate("http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/channels/splashScreen.html");
        }

        void tvBrowser_Navigated(object sender, NavigationEventArgs e)
        {
            SetSilent(tvBrowser, true);
        }

        private void image1_ImageFailed(object sender, ExceptionRoutedEventArgs e)
        {

        }

        private void kinectColorViewer1_Loaded(object sender, RoutedEventArgs e)
        {

        }

        private void showImageHoverRegion_Loaded(object sender, RoutedEventArgs e)
        {

        }
        #endregion additional



        //Code to supress Java Script errors in web browser
        public static void SetSilent(WebBrowser browser, bool silent)
        {
            if (browser == null)
                throw new ArgumentNullException("browser");

            // get an IWebBrowser2 from the document
            IOleServiceProvider sp = browser.Document as IOleServiceProvider;
            if (sp != null)
            {
                Guid IID_IWebBrowserApp = new Guid("0002DF05-0000-0000-C000-000000000046");
                Guid IID_IWebBrowser2 = new Guid("D30C1661-CDAF-11d0-8A3E-00C04FC9E26E");

                object webBrowser;
                sp.QueryService(ref IID_IWebBrowserApp, ref IID_IWebBrowser2, out webBrowser);
                if (webBrowser != null)
                {
                    webBrowser.GetType().InvokeMember("Silent", BindingFlags.Instance | BindingFlags.Public | BindingFlags.PutDispProperty, null, webBrowser, new object[] { silent });
                }
            }
        }

        [ComImport, Guid("6D5140C1-7436-11CE-8034-00AA006009FA"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        private interface IOleServiceProvider
        {
            [PreserveSig]
            int QueryService([In] ref Guid guidService, [In] ref Guid riid, [MarshalAs(UnmanagedType.IDispatch)] out object ppvObject);
        }






        //Code to get assemblies when running as a single executable
        static Assembly ResolveAssembly(object sender, ResolveEventArgs args)
        {
            Console.WriteLine("Resolving assembly...");
            Assembly parentAssembly = Assembly.GetExecutingAssembly();

            String parentName = parentAssembly.ToString().Substring(0, parentAssembly.ToString().IndexOf(','));

            var temp = args.Name.Substring(0, args.Name.IndexOf(','))/* + ".dll"*/;
            var name = parentName+".dll."+temp.Substring(0, temp.LastIndexOf('.')) + ".dll";
            Console.WriteLine(name);

            string[] names = parentAssembly.GetManifestResourceNames();
            for (int i = 0; i < names.Length; i++)
            {
                Console.WriteLine(names[i]);
            }

            var resourceName = parentAssembly.GetManifestResourceNames()
                .First(s => s.EndsWith(name));
            //Console.WriteLine(resourceName);

            using (Stream stream = parentAssembly.GetManifestResourceStream(resourceName))
            {
                byte[] block = new byte[stream.Length];
                stream.Read(block, 0, block.Length);
                Console.WriteLine("Loading..." + block.ToString());
                return Assembly.Load(block);
            }
        }

    }
}
