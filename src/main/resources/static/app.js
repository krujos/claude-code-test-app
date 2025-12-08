const { useState, useEffect, useRef } = React;
const { parsePhoneNumber, AsYouType } = window.libphonenumber;

// Common countries with their calling codes and example formats
const COUNTRIES = [
    { code: 'US', name: 'United States', flag: '🇺🇸', callingCode: '+1', placeholder: '(201) 555-0123' },
    { code: 'CA', name: 'Canada', flag: '🇨🇦', callingCode: '+1', placeholder: '(204) 555-0123' },
    { code: 'GB', name: 'United Kingdom', flag: '🇬🇧', callingCode: '+44', placeholder: '020 7123 4567' },
    { code: 'DE', name: 'Germany', flag: '🇩🇪', callingCode: '+49', placeholder: '030 12345678' },
    { code: 'FR', name: 'France', flag: '🇫🇷', callingCode: '+33', placeholder: '01 23 45 67 89' },
    { code: 'IT', name: 'Italy', flag: '🇮🇹', callingCode: '+39', placeholder: '02 1234 5678' },
    { code: 'ES', name: 'Spain', flag: '🇪🇸', callingCode: '+34', placeholder: '912 34 56 78' },
    { code: 'MX', name: 'Mexico', flag: '🇲🇽', callingCode: '+52', placeholder: '55 1234 5678' },
    { code: 'JP', name: 'Japan', flag: '🇯🇵', callingCode: '+81', placeholder: '03-1234-5678' },
    { code: 'AU', name: 'Australia', flag: '🇦🇺', callingCode: '+61', placeholder: '02 1234 5678' },
];

function CountryDropdown({ value, onChange }) {
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef(null);

    const selectedCountry = COUNTRIES.find(c => c.code === value) || COUNTRIES[0];

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };

        if (isOpen) {
            document.addEventListener('mousedown', handleClickOutside);
            return () => document.removeEventListener('mousedown', handleClickOutside);
        }
    }, [isOpen]);

    const handleSelect = (countryCode) => {
        onChange(countryCode);
        setIsOpen(false);
    };

    return (
        <div className="country-dropdown" ref={dropdownRef}>
            <button
                type="button"
                className="country-dropdown-button"
                onClick={() => setIsOpen(!isOpen)}
            >
                {selectedCountry.callingCode}
            </button>
            {isOpen && (
                <div className="country-dropdown-menu">
                    {COUNTRIES.map(country => (
                        <div
                            key={country.code}
                            className={`country-dropdown-item ${country.code === value ? 'selected' : ''}`}
                            onClick={() => handleSelect(country.code)}
                        >
                            {country.flag} {country.name} ({country.callingCode})
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

function PhoneInput({ value, onChange, country, onCountryChange, error, setError }) {
    const [displayValue, setDisplayValue] = useState('');

    useEffect(() => {
        // Initialize display value when value changes from outside
        if (value && !displayValue) {
            setDisplayValue(value);
        }
    }, [value]);

    const formatPhoneNumber = (input, countryCode) => {
        try {
            const formatter = new AsYouType(countryCode);
            const formatted = formatter.input(input);
            return formatted;
        } catch (e) {
            return input;
        }
    };

    const validatePhoneNumber = (phoneNumber, countryCode) => {
        // Empty phone numbers should allow typing but not try to convert
        if (!phoneNumber || phoneNumber.trim().length === 0) {
            return { isValid: false, error: null, isEmpty: true };
        }

        try {
            const parsed = parsePhoneNumber(phoneNumber, countryCode);
            if (parsed && parsed.isValid()) {
                return { isValid: true, error: null, isEmpty: false };
            } else {
                return {
                    isValid: false,
                    error: `Invalid ${COUNTRIES.find(c => c.code === countryCode)?.name || countryCode} phone number format`,
                    isEmpty: false
                };
            }
        } catch (e) {
            const country = COUNTRIES.find(c => c.code === countryCode);
            const placeholder = country ? country.placeholder : '';
            return {
                isValid: false,
                error: `Invalid phone number. Expected format: ${placeholder}`,
                isEmpty: false
            };
        }
    };

    const handlePhoneChange = (e) => {
        const input = e.target.value;

        // Extract only digits from the input
        const digitsOnly = input.replace(/\D/g, '');

        // Format as user types
        const formatted = formatPhoneNumber(digitsOnly, country);
        setDisplayValue(formatted);

        // Validate the digits only
        const validation = validatePhoneNumber(digitsOnly, country);

        // Handle empty phone numbers - just store as-is
        if (validation.isEmpty) {
            setError(null);
            onChange(formatted);
            return;
        }

        if (validation.isValid) {
            setError(null);
            // Store in international format for backend
            try {
                const parsed = parsePhoneNumber(formatted, country);
                if (parsed && parsed.isValid()) {
                    // Send international format to backend (e.g., "+1 212 456 7890")
                    const international = parsed.format('INTERNATIONAL');
                    onChange(international);
                } else {
                    onChange(formatted);
                }
            } catch (e) {
                onChange(formatted);
            }
        } else {
            setError(validation.error);
            // Still update the value so user can see what they're typing
            onChange(formatted);
        }
    };

    const handleCountryChange = (newCountry) => {
        onCountryChange(newCountry);

        // Re-validate with new country and update international format
        if (displayValue) {
            const validation = validatePhoneNumber(displayValue, newCountry);
            setError(validation.error);

            // Update the stored value to international format
            if (validation.isValid) {
                try {
                    const parsed = parsePhoneNumber(displayValue, newCountry);
                    if (parsed && parsed.isValid()) {
                        onChange(parsed.format('INTERNATIONAL'));
                    }
                } catch (e) {
                    // Keep current value if parsing fails
                }
            }
        }
    };

    const getPlaceholder = () => {
        const countryData = COUNTRIES.find(c => c.code === country);
        return countryData ? countryData.placeholder : '(###) ###-####';
    };

    return (
        <div className="phone-input-group">
            <div className="form-group" style={{marginBottom: 0}}>
                <label>Country</label>
                <CountryDropdown value={country} onChange={handleCountryChange} />
            </div>
            <div className="form-group" style={{marginBottom: 0}}>
                <label>Phone Number</label>
                <input
                    type="tel"
                    value={displayValue}
                    onChange={handlePhoneChange}
                    placeholder={getPlaceholder()}
                    className={error ? 'error' : ''}
                    required
                />
            </div>
        </div>
    );
}

function AddressAutocomplete({ value, onChange }) {
    const inputRef = useRef(null);
    const autocompleteRef = useRef(null);
    const [isLoaded, setIsLoaded] = useState(false);

    useEffect(() => {
        // Wait for Google Maps to load
        const checkGoogleMaps = () => {
            if (window.google && window.google.maps && window.google.maps.places) {
                setIsLoaded(true);
            } else {
                setTimeout(checkGoogleMaps, 100);
            }
        };
        checkGoogleMaps();
    }, []);

    useEffect(() => {
        if (!inputRef.current || !isLoaded || !window.google) return;

        // Initialize Google Places Autocomplete
        autocompleteRef.current = new window.google.maps.places.Autocomplete(
            inputRef.current,
            {
                types: ['address'],
                fields: ['formatted_address', 'address_components', 'geometry']
            }
        );

        // Listen for place selection
        autocompleteRef.current.addListener('place_changed', () => {
            const place = autocompleteRef.current.getPlace();
            if (place.formatted_address) {
                onChange(place.formatted_address);
            }
        });

        return () => {
            if (autocompleteRef.current) {
                window.google.maps.event.clearInstanceListeners(autocompleteRef.current);
            }
        };
    }, [isLoaded]);

    // Update input value when value prop changes
    useEffect(() => {
        if (inputRef.current && value !== inputRef.current.value) {
            inputRef.current.value = value;
        }
    }, [value]);

    const handleChange = (e) => {
        onChange(e.target.value);
    };

    return (
        <input
            ref={inputRef}
            type="text"
            value={value}
            onChange={handleChange}
            placeholder="Start typing an address..."
            required
        />
    );
}

function App() {
    const [activeTab, setActiveTab] = useState('create');
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        phoneNumber: '',
        address: '',
        apartmentNumber: ''
    });
    const [country, setCountry] = useState('US');
    const [phoneError, setPhoneError] = useState(null);
    const [adminPassword, setAdminPassword] = useState('');
    const [accounts, setAccounts] = useState([]);
    const [message, setMessage] = useState(null);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [editingId, setEditingId] = useState(null);

    // SSE for live updates
    useEffect(() => {
        const eventSource = new EventSource('/api/accounts/stream');

        eventSource.onmessage = (event) => {
            const account = JSON.parse(event.data);
            console.log('Received account:', account);
        };

        eventSource.onerror = (error) => {
            console.log('SSE Error:', error);
            eventSource.close();
        };

        return () => eventSource.close();
    }, []);

    const handleInputChange = (e) => {
        setFormData(prevFormData => ({
            ...prevFormData,
            [e.target.name]: e.target.value
        }));
    };

    const handlePhoneChange = (value) => {
        setFormData(prevFormData => ({
            ...prevFormData,
            phoneNumber: value
        }));
    };

    const createAccount = async (e) => {
        e.preventDefault();

        // Final validation before submit
        if (phoneError) {
            setMessage({ type: 'error', text: 'Please fix phone number errors before submitting' });
            setTimeout(() => setMessage(null), 5000);
            return;
        }

        // Check for empty phone number
        if (!formData.phoneNumber || formData.phoneNumber.trim() === '') {
            setMessage({ type: 'error', text: 'Phone number is required' });
            setTimeout(() => setMessage(null), 5000);
            return;
        }

        try {
            const response = await fetch('/api/accounts', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                const newAccount = await response.json();
                setMessage({ type: 'success', text: `Account created successfully! ID: ${newAccount.id}` });
                setFormData({
                    firstName: '',
                    lastName: '',
                    phoneNumber: '',
                    address: '',
                    apartmentNumber: ''
                });
                setPhoneError(null);
            } else {
                const errorText = await response.text();
                setMessage({ type: 'error', text: 'Failed to create account: ' + errorText });
            }
        } catch (error) {
            setMessage({ type: 'error', text: 'Error: ' + error.message });
        }

        setTimeout(() => setMessage(null), 5000);
    };

    const loginAdmin = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('/api/admin/accounts', {
                headers: {
                    'Authorization': 'Basic ' + btoa('admin:' + adminPassword)
                }
            });

            if (response.ok) {
                const data = await response.json();
                setAccounts(data);
                setIsAuthenticated(true);
                setMessage({ type: 'success', text: 'Login successful!' });
            } else {
                setMessage({ type: 'error', text: 'Invalid password' });
            }
        } catch (error) {
            setMessage({ type: 'error', text: 'Login failed: ' + error.message });
        }

        setTimeout(() => setMessage(null), 3000);
    };

    const updateAccount = async (id) => {
        if (phoneError) {
            setMessage({ type: 'error', text: 'Please fix phone number errors before updating' });
            setTimeout(() => setMessage(null), 5000);
            return;
        }

        try {
            const response = await fetch(`/api/admin/accounts/${id}`, {
                method: 'PUT',
                headers: {
                    'Authorization': 'Basic ' + btoa('admin:' + adminPassword),
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                const updatedAccount = await response.json();
                setAccounts(accounts.map(acc => acc.id === id ? updatedAccount : acc));
                setEditingId(null);
                setFormData({
                    firstName: '',
                    lastName: '',
                    phoneNumber: '',
                    address: '',
                    apartmentNumber: ''
                });
                setPhoneError(null);
                setMessage({ type: 'success', text: 'Account updated successfully!' });
            } else {
                const errorText = await response.text();
                setMessage({ type: 'error', text: 'Update failed: ' + errorText });
            }
        } catch (error) {
            setMessage({ type: 'error', text: 'Update failed: ' + error.message });
        }

        setTimeout(() => setMessage(null), 3000);
    };

    const deleteAccount = async (id) => {
        if (!confirm('Are you sure you want to delete this account?')) return;

        try {
            const response = await fetch(`/api/admin/accounts/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': 'Basic ' + btoa('admin:' + adminPassword)
                }
            });

            if (response.ok) {
                setAccounts(accounts.filter(acc => acc.id !== id));
                setMessage({ type: 'success', text: 'Account deleted successfully!' });
            }
        } catch (error) {
            setMessage({ type: 'error', text: 'Delete failed: ' + error.message });
        }

        setTimeout(() => setMessage(null), 3000);
    };

    const startEdit = (account) => {
        setEditingId(account.id);
        setFormData({
            firstName: account.firstName,
            lastName: account.lastName,
            phoneNumber: account.phoneNumber,
            address: account.address,
            apartmentNumber: account.apartmentNumber || ''
        });
        setPhoneError(null);
    };

    const cancelEdit = () => {
        setEditingId(null);
        setFormData({
            firstName: '',
            lastName: '',
            phoneNumber: '',
            address: '',
            apartmentNumber: ''
        });
        setPhoneError(null);
    };

    return (
        <div className="container">
            <div className="header">
                <div className="logo">GULF RACING</div>
                <div className="tagline">Account Management System</div>
            </div>

            <div className="racing-stripe"></div>

            <div className="tab-container">
                <div
                    className={`tab ${activeTab === 'create' ? 'active' : ''}`}
                    onClick={() => setActiveTab('create')}
                >
                    Create Account
                </div>
                <div
                    className={`tab ${activeTab === 'admin' ? 'active' : ''}`}
                    onClick={() => setActiveTab('admin')}
                >
                    Admin Dashboard
                </div>
            </div>

            {message && (
                <div className={message.type === 'success' ? 'success-message' : 'error-message'}>
                    {message.text}
                </div>
            )}

            {activeTab === 'create' && (
                <div className="card">
                    <h2 style={{marginBottom: '1.5rem', color: 'var(--gulf-blue)', fontFamily: "'Racing Sans One', cursive"}}>
                        Register Your Account
                    </h2>
                    <form onSubmit={createAccount}>
                        <div className="form-group">
                            <label>First Name</label>
                            <input
                                type="text"
                                name="firstName"
                                value={formData.firstName}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label>Last Name</label>
                            <input
                                type="text"
                                name="lastName"
                                value={formData.lastName}
                                onChange={handleInputChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <PhoneInput
                                value={formData.phoneNumber}
                                onChange={handlePhoneChange}
                                country={country}
                                onCountryChange={setCountry}
                                error={phoneError}
                                setError={setPhoneError}
                            />
                            {phoneError && (
                                <div className="field-error">{phoneError}</div>
                            )}
                        </div>
                        <div className="form-group">
                            <label>Address</label>
                            <AddressAutocomplete
                                value={formData.address}
                                onChange={(address) => setFormData(prevFormData => ({...prevFormData, address}))}
                            />
                        </div>
                        <div className="form-group">
                            <label>Apartment / Unit # (optional)</label>
                            <input
                                type="text"
                                name="apartmentNumber"
                                value={formData.apartmentNumber}
                                onChange={handleInputChange}
                                placeholder="e.g., Apt 4B, Unit 202"
                            />
                        </div>
                        <button type="submit" className="btn btn-primary" disabled={phoneError !== null}>
                            Create Account
                        </button>
                    </form>
                </div>
            )}

            {activeTab === 'admin' && (
                <div className="card">
                    {!isAuthenticated ? (
                        <>
                            <h2 style={{marginBottom: '1.5rem', color: 'var(--gulf-blue)', fontFamily: "'Racing Sans One', cursive"}}>
                                Admin Login
                            </h2>
                            <form onSubmit={loginAdmin}>
                                <div className="form-group">
                                    <label>Password</label>
                                    <input
                                        type="password"
                                        value={adminPassword}
                                        onChange={(e) => setAdminPassword(e.target.value)}
                                        required
                                        placeholder="Enter admin password"
                                    />
                                </div>
                                <button type="submit" className="btn btn-secondary">
                                    Login
                                </button>
                            </form>
                        </>
                    ) : (
                        <>
                            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem'}}>
                                <h2 style={{color: 'var(--gulf-blue)', fontFamily: "'Racing Sans One', cursive", margin: 0}}>
                                    Manage Accounts
                                </h2>
                                <button
                                    onClick={() => {
                                        setIsAuthenticated(false);
                                        setAdminPassword('');
                                        setAccounts([]);
                                    }}
                                    className="btn btn-primary"
                                    style={{padding: '0.5rem 1.5rem', fontSize: '0.9rem'}}
                                >
                                    Logout
                                </button>
                            </div>
                            <div className="account-list">
                                {accounts.map(account => (
                                    <div key={account.id} className={`account-item ${editingId === account.id ? 'edit-mode' : ''}`}>
                                        {editingId === account.id ? (
                                            <div>
                                                <div className="form-group">
                                                    <label>First Name</label>
                                                    <input
                                                        type="text"
                                                        name="firstName"
                                                        value={formData.firstName}
                                                        onChange={handleInputChange}
                                                    />
                                                </div>
                                                <div className="form-group">
                                                    <label>Last Name</label>
                                                    <input
                                                        type="text"
                                                        name="lastName"
                                                        value={formData.lastName}
                                                        onChange={handleInputChange}
                                                    />
                                                </div>
                                                <div className="form-group">
                                                    <PhoneInput
                                                        value={formData.phoneNumber}
                                                        onChange={handlePhoneChange}
                                                        country={country}
                                                        onCountryChange={setCountry}
                                                        error={phoneError}
                                                        setError={setPhoneError}
                                                    />
                                                    {phoneError && (
                                                        <div className="field-error">{phoneError}</div>
                                                    )}
                                                </div>
                                                <div className="form-group">
                                                    <label>Address</label>
                                                    <AddressAutocomplete
                                                        value={formData.address}
                                                        onChange={(address) => setFormData(prevFormData => ({...prevFormData, address}))}
                                                    />
                                                </div>
                                                <div className="form-group">
                                                    <label>Apartment / Unit # (optional)</label>
                                                    <input
                                                        type="text"
                                                        name="apartmentNumber"
                                                        value={formData.apartmentNumber}
                                                        onChange={handleInputChange}
                                                        placeholder="e.g., Apt 4B, Unit 202"
                                                    />
                                                </div>
                                                <div className="button-group">
                                                    <button
                                                        onClick={() => updateAccount(account.id)}
                                                        className="btn btn-secondary"
                                                        style={{flex: 1}}
                                                        disabled={phoneError !== null}
                                                    >
                                                        Save
                                                    </button>
                                                    <button
                                                        onClick={cancelEdit}
                                                        className="btn btn-primary"
                                                        style={{flex: 1}}
                                                    >
                                                        Cancel
                                                    </button>
                                                </div>
                                            </div>
                                        ) : (
                                            <>
                                                <div className="account-info">
                                                    <div>
                                                        <div className="info-label">Name</div>
                                                        <div className="info-value">{account.firstName} {account.lastName}</div>
                                                    </div>
                                                    <div>
                                                        <div className="info-label">Phone</div>
                                                        <div className="info-value">{account.phoneNumber}</div>
                                                    </div>
                                                    <div>
                                                        <div className="info-label">Address</div>
                                                        <div className="info-value">
                                                            {account.address}
                                                            {account.apartmentNumber && `, ${account.apartmentNumber}`}
                                                        </div>
                                                    </div>
                                                    <div>
                                                        <div className="info-label">ID</div>
                                                        <div className="info-value">#{account.id}</div>
                                                    </div>
                                                </div>
                                                <div className="button-group">
                                                    <button
                                                        onClick={() => startEdit(account)}
                                                        className="btn btn-secondary"
                                                        style={{flex: 1}}
                                                    >
                                                        Edit
                                                    </button>
                                                    <button
                                                        onClick={() => deleteAccount(account.id)}
                                                        className="btn btn-primary"
                                                        style={{flex: 1}}
                                                    >
                                                        Delete
                                                    </button>
                                                </div>
                                            </>
                                        )}
                                    </div>
                                ))}
                                {accounts.length === 0 && (
                                    <p style={{textAlign: 'center', color: 'var(--gulf-blue)', fontSize: '1.2rem'}}>
                                        No accounts yet. Create one in the other tab!
                                    </p>
                                )}
                            </div>
                        </>
                    )}
                </div>
            )}
        </div>
    );
}

ReactDOM.render(<App />, document.getElementById('root'));
