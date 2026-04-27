import { useState, useEffect, useCallback } from 'react';

interface Location {
  lat: number;
  lng: number;
}

interface GeolocationState {
  location: Location | null;
  loading: boolean;
  error: string | null;
}

export default function useGeolocation() {
  const [state, setState] = useState<GeolocationState>({
    location: null,
    loading: false,
    error: null,
  });

  // Load saved location from localStorage
  useEffect(() => {
    const savedLocation = localStorage.getItem('userLocation');
    if (savedLocation) {
      try {
        const location = JSON.parse(savedLocation);
        setState(prev => ({ ...prev, location }));
      } catch (error) {
        console.error('Failed to parse saved location:', error);
      }
    }
  }, []);

  const requestLocation = useCallback(() => {
    if (!navigator.geolocation) {
      setState(prev => ({
        ...prev,
        error: 'Geolocation is not supported by this browser',
        loading: false,
      }));
      return;
    }

    setState(prev => ({ ...prev, loading: true, error: null }));

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const location = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        };

        // Save to localStorage
        localStorage.setItem('userLocation', JSON.stringify(location));

        setState({
          location,
          loading: false,
          error: null,
        });
      },
      (error) => {
        let errorMessage = 'Unable to retrieve your location';

        switch (error.code) {
          case error.PERMISSION_DENIED:
            errorMessage = 'Location access denied by user';
            break;
          case error.POSITION_UNAVAILABLE:
            errorMessage = 'Location information is unavailable';
            break;
          case error.TIMEOUT:
            errorMessage = 'Location request timed out';
            break;
        }

        setState(prev => ({
          ...prev,
          error: errorMessage,
          loading: false,
        }));
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 300000, // 5 minutes
      }
    );
  }, []);

  // Auto-request location on mount if not already available
  useEffect(() => {
    if (!state.location && !state.loading) {
      requestLocation();
    }
  }, [state.location, state.loading, requestLocation]);

  return {
    location: state.location,
    loading: state.loading,
    error: state.error,
    requestLocation,
  };
}