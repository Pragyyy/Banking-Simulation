
import React, { useState, useEffect } from 'react';
import { User, LogOut, Home, Users, CreditCard, ArrowLeftRight, Eye, EyeOff, Mail, Phone, Calendar, Building2, Hash, Wallet, Send, Download, Search, Filter, X, Menu, ChevronDown, UserPlus, Edit } from 'lucide-react';

// API Base URL
const API_BASE_URL = 'http://localhost:8080/Banking_Simulation/api';

// Hardcoded Admin Credentials
const ADMIN_CREDENTIALS = {
  username: 'admin',
  password: 'admin123'
};

// API Service
const api = {
  // Customer APIs
  createCustomer: async (customer) => {
    const response = await fetch(`${API_BASE_URL}/customer/onboard`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(customer)
    });
    return response.json();
  },
  getCustomerByAadhar: async (aadhar) => {
    const response = await fetch(`${API_BASE_URL}/customer/aadhar/${aadhar}`);
    return response.json();
  },
  getAllCustomers: async () => {
    const response = await fetch(`${API_BASE_URL}/customer/all`);
    return response.json();
  },
  getCustomer: async (customerId) => {
    const response = await fetch(`${API_BASE_URL}/customer/${customerId}`);
    return response.json();
  },
  updateCustomer: async (customerId, customerData) => {
    const response = await fetch(`${API_BASE_URL}/customer/${customerId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(customerData)
    });
    return response.json();
  },

  // Account APIs
  createAccount: async (account) => {
    const response = await fetch(`${API_BASE_URL}/account/create`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(account)
    });
    return response.json();
  },
  getAllAccounts: async () => {
    const response = await fetch(`${API_BASE_URL}/account/all`);
    return response.json();
  },
  getAccountsByAadhar: async (aadhar) => {
    const response = await fetch(`${API_BASE_URL}/account/aadhar/${aadhar}`);
    return response.json();
  },
  getAccountByNumber: async (accountNumber) => {
    const response = await fetch(`${API_BASE_URL}/account/${accountNumber}`);
    return response.json();
  },
  updateAccount: async (accountNumber, accountData) => {
    const response = await fetch(`${API_BASE_URL}/account/${accountNumber}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(accountData)
    });
    return response.json();
  },

  // Transaction APIs
  processTransaction: async (transactionData) => {
    const response = await fetch(`${API_BASE_URL}/transaction/process`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(transactionData)
    });
    return response.json();
  },
  getTransactionsByAccount: async (accountNumber) => {
    const response = await fetch(`${API_BASE_URL}/transaction/${accountNumber}`);
    return response.json();
  },
  getAllTransactions: async () => {
    const response = await fetch(`${API_BASE_URL}/transaction/all`);
    return response.json();
  }
};

// Main App Component
export default function BankingApp() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userType, setUserType] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [activeView, setActiveView] = useState('dashboard');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const handleLogin = (type, userData) => {
    setIsLoggedIn(true);
    setUserType(type);
    setCurrentUser(userData);
    setActiveView('dashboard');
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
    setUserType(null);
    setCurrentUser(null);
    setActiveView('dashboard');
  };

  if (!isLoggedIn) {
    return <LoginPage onLogin={handleLogin} />;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* Header */}
      <header className="bg-white shadow-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-3">
              <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-2 rounded-lg">
                <Building2 className="text-white" size={28} />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">SecureBank</h1>
                <p className="text-xs text-gray-500">Banking Simulation</p>
              </div>
            </div>
            
            {/* Mobile Menu Button */}
            <button 
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="lg:hidden p-2 rounded-lg hover:bg-gray-100"
            >
              <Menu size={24} />
            </button>

            {/* Desktop User Info */}
            <div className="hidden lg:flex items-center space-x-4">
              <div className="text-right">
                <p className="text-sm font-semibold text-gray-900">
                  {userType === 'admin' ? 'Administrator' : currentUser?.name}
                </p>
                <p className="text-xs text-gray-500">
                  {userType === 'admin' ? 'Admin Panel' : 'Customer Portal'}
                </p>
              </div>
              <button
                onClick={handleLogout}
                className="flex items-center space-x-2 bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition-colors"
              >
                <LogOut size={18} />
                <span>Logout</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Mobile Menu */}
      {isMobileMenuOpen && (
        <div className="lg:hidden fixed inset-0 z-50 bg-black bg-opacity-50" onClick={() => setIsMobileMenuOpen(false)}>
          <div className="bg-white w-64 h-full shadow-xl p-6" onClick={(e) => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-lg font-bold">Menu</h2>
              <button onClick={() => setIsMobileMenuOpen(false)}>
                <X size={24} />
              </button>
            </div>
            <div className="space-y-2">
              {userType === 'admin' ? (
                <>
                  <button onClick={() => { setActiveView('dashboard'); setIsMobileMenuOpen(false); }} className="w-full text-left px-4 py-2 rounded hover:bg-gray-100">Dashboard</button>
                  <button onClick={() => { setActiveView('customers'); setIsMobileMenuOpen(false); }} className="w-full text-left px-4 py-2 rounded hover:bg-gray-100">View Customers</button>
                  <button onClick={() => { setActiveView('accounts'); setIsMobileMenuOpen(false); }} className="w-full text-left px-4 py-2 rounded hover:bg-gray-100">View Accounts</button>
                  <button onClick={() => { setActiveView('transactions'); setIsMobileMenuOpen(false); }} className="w-full text-left px-4 py-2 rounded hover:bg-gray-100">View Transactions</button>
                </>
              ) : (
                <>
                  <button onClick={() => { setActiveView('dashboard'); setIsMobileMenuOpen(false); }} className="w-full text-left px-4 py-2 rounded hover:bg-gray-100">Dashboard</button>
                  <button onClick={() => { setActiveView('profile'); setIsMobileMenuOpen(false); }} className="w-full text-left px-4 py-2 rounded hover:bg-gray-100">My Profile</button>
                  <button onClick={() => { setActiveView('accounts'); setIsMobileMenuOpen(false); }} className="w-full text-left px-4 py-2 rounded hover:bg-gray-100">My Accounts</button>
                  <button onClick={() => { setActiveView('createAccount'); setIsMobileMenuOpen(false); }} className="w-full text-left px-4 py-2 rounded hover:bg-gray-100">Create Account</button>
                  <button onClick={() => { setActiveView('transfer'); setIsMobileMenuOpen(false); }} className="w-full text-left px-4 py-2 rounded hover:bg-gray-100">Transfer Money</button>
                  <button onClick={() => { setActiveView('transactions'); setIsMobileMenuOpen(false); }} className="w-full text-left px-4 py-2 rounded hover:bg-gray-100">Transactions</button>
                </>
              )}
              <button onClick={handleLogout} className="w-full text-left px-4 py-2 rounded hover:bg-red-100 text-red-600">Logout</button>
            </div>
          </div>
        </div>
      )}

      <div className="flex max-w-7xl mx-auto">
        {/* Sidebar - Hidden on Mobile */}
        <aside className="hidden lg:block w-64 bg-white shadow-lg min-h-screen p-6">
          <nav className="space-y-2">
            {userType === 'admin' ? (
              <>
                <NavButton icon={Home} label="Dashboard" active={activeView === 'dashboard'} onClick={() => setActiveView('dashboard')} />
                <NavButton icon={Users} label="View Customers" active={activeView === 'customers'} onClick={() => setActiveView('customers')} />
                <NavButton icon={CreditCard} label="View Accounts" active={activeView === 'accounts'} onClick={() => setActiveView('accounts')} />
                <NavButton icon={ArrowLeftRight} label="View Transactions" active={activeView === 'transactions'} onClick={() => setActiveView('transactions')} />
              </>
            ) : (
              <>
                <NavButton icon={Home} label="Dashboard" active={activeView === 'dashboard'} onClick={() => setActiveView('dashboard')} />
                <NavButton icon={User} label="My Profile" active={activeView === 'profile'} onClick={() => setActiveView('profile')} />
                <NavButton icon={CreditCard} label="My Accounts" active={activeView === 'accounts'} onClick={() => setActiveView('accounts')} />
                <NavButton icon={UserPlus} label="Create Account" active={activeView === 'createAccount'} onClick={() => setActiveView('createAccount')} />
                <NavButton icon={Send} label="Transfer Money" active={activeView === 'transfer'} onClick={() => setActiveView('transfer')} />
                <NavButton icon={ArrowLeftRight} label="Transactions" active={activeView === 'transactions'} onClick={() => setActiveView('transactions')} />
              </>
            )}
          </nav>
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-4 lg:p-8">
          {userType === 'admin' ? (
            <AdminPanel activeView={activeView} />
          ) : (
            <CustomerPanel activeView={activeView} currentUser={currentUser} />
          )}
        </main>
      </div>
    </div>
  );
}

// Navigation Button Component
function NavButton({ icon: Icon, label, active, onClick }) {
  return (
    <button
      onClick={onClick}
      className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg transition-all ${
        active
          ? 'bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-lg'
          : 'text-gray-700 hover:bg-gray-100'
      }`}
    >
      <Icon size={20} />
      <span className="font-medium">{label}</span>
    </button>
  );
}

// Login Page Component
function LoginPage({ onLogin }) {
  const [loginType, setLoginType] = useState('customer');
  const [showRegister, setShowRegister] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    aadhar: '',
    pin: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (loginType === 'admin') {
        if (
          formData.username === ADMIN_CREDENTIALS.username &&
          formData.password === ADMIN_CREDENTIALS.password
        ) {
          onLogin('admin', { username: formData.username });
        } else {
          setError('Invalid admin credentials');
        }
      } else {
        const accountsResponse = await api.getAccountsByAadhar(formData.aadhar);
        let accounts = [];

        if (accountsResponse.success && accountsResponse.data) {
          accounts = accountsResponse.data;
        }

        const customerResponse = await api.getCustomerByAadhar(formData.aadhar);

        if (customerResponse.success) {
          const customer = customerResponse.data;

          if (customer.customerPin === formData.pin) {
            onLogin('customer', { ...customer, accounts });
          } else {
            setError('Incorrect PIN');
          }
        } else {
          setError('Customer not found. Please register first.');
        }
      }
    } catch (err) {
      console.error('Login error:', err);
      setError('Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (showRegister && loginType === 'customer') {
    return <RegisterPage onBack={() => setShowRegister(false)} onRegisterSuccess={() => setShowRegister(false)} />;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-600 via-indigo-600 to-purple-700 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden">
        <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-8 text-white text-center">
          <div className="bg-white/20 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-4">
            <Building2 size={40} />
          </div>
          <h1 className="text-3xl font-bold mb-2">SecureBank</h1>
          <p className="text-blue-100">Banking Simulation System</p>
        </div>

        <div className="flex border-b">
          <button
            onClick={() => setLoginType('customer')}
            className={`flex-1 py-4 font-semibold transition-all ${
              loginType === 'customer'
                ? 'bg-blue-50 text-blue-600 border-b-2 border-blue-600'
                : 'text-gray-500 hover:bg-gray-50'
            }`}
          >
            Customer Login
          </button>
          <button
            onClick={() => setLoginType('admin')}
            className={`flex-1 py-4 font-semibold transition-all ${
              loginType === 'admin'
                ? 'bg-blue-50 text-blue-600 border-b-2 border-blue-600'
                : 'text-gray-500 hover:bg-gray-50'
            }`}
          >
            Admin Login
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-8 space-y-6">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
              {error}
            </div>
          )}

          {loginType === 'admin' ? (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Username
                </label>
                <input
                  type="text"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Enter username"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Password
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="Enter password"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500"
                  >
                    {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                  </button>
                </div>
              </div>
            </>
          ) : (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Aadhar Number
                </label>
                <input
                  type="text"
                  value={formData.aadhar}
                  onChange={(e) => setFormData({ ...formData, aadhar: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Enter 12-digit Aadhar"
                  maxLength="12"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  PIN
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={formData.pin}
                    onChange={(e) => setFormData({ ...formData, pin: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="Enter 4-6 digit PIN"
                    maxLength="6"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500"
                  >
                    {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                  </button>
                </div>
              </div>
            </>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-3 rounded-lg font-semibold hover:shadow-lg transform hover:scale-105 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>

          {loginType === 'customer' && (
            <div className="text-center">
              <button
                type="button"
                onClick={() => setShowRegister(true)}
                className="text-blue-600 hover:text-blue-800 font-semibold"
              >
                New Customer? Register Here
              </button>
            </div>
          )}
        </form>

        <div className="bg-gray-50 px-8 py-4 border-t">
          <p className="text-xs text-gray-600 text-center">
            {loginType === 'admin' 
              ? 'Demo: admin / admin123' 
              : 'Register as new customer or login with your Aadhar & PIN'}
          </p>
        </div>
      </div>
    </div>
  );
}

// Register Page Component
function RegisterPage({ onBack, onRegisterSuccess }) {
  const [formData, setFormData] = useState({
    name: '',
    phoneNumber: '',
    email: '',
    address: '',
    customerPin: '',
    aadharNumber: '',
    dob: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await api.createCustomer(formData);
      
      if (response.success) {
        setSuccess(true);
        setTimeout(() => {
          onRegisterSuccess();
        }, 2000);
      } else {
        setError(response.message || 'Registration failed');
      }
    } catch (err) {
      setError('Registration failed. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-600 via-indigo-600 to-purple-700 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl overflow-hidden">
        <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-6 text-white flex justify-between items-center">
          <h3 className="text-2xl font-bold">Register New Customer</h3>
          <button onClick={onBack} className="hover:bg-white/20 p-2 rounded-lg transition-colors">
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-8 space-y-4 max-h-[80vh] overflow-y-auto">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
              {error}
            </div>
          )}

          {success && (
            <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
              Registration successful! Redirecting to login...
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Full Name *
              </label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="John Doe"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Phone Number * (10 digits)
              </label>
              <input
                type="tel"
                value={formData.phoneNumber}
                onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="9876543210"
                maxLength="10"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Email Address *
              </label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="john@example.com"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Aadhar Number * (12 digits)
              </label>
              <input
                type="text"
                value={formData.aadharNumber}
                onChange={(e) => setFormData({ ...formData, aadharNumber: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="123456789012"
                maxLength="12"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Date of Birth * (Must be 18+)
              </label>
              <input
                type="date"
                value={formData.dob}
                onChange={(e) => setFormData({ ...formData, dob: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                PIN * (4-6 digits)
              </label>
              <input
                type="password"
                value={formData.customerPin}
                onChange={(e) => setFormData({ ...formData, customerPin: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="Enter PIN"
                maxLength="6"
                required
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Address
            </label>
            <textarea
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              placeholder="Enter complete address"
              rows="3"
            />
          </div>

          <div className="flex space-x-4 pt-4">
            <button
              type="button"
              onClick={onBack}
              className="flex-1 bg-gray-200 text-gray-800 py-3 rounded-lg font-semibold hover:bg-gray-300 transition-colors"
            >
              Back to Login
            </button>
            <button
              type="submit"
              disabled={loading || success}
              className="flex-1 bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-3 rounded-lg font-semibold hover:shadow-lg transition-all disabled:opacity-50"
            >
              {loading ? 'Registering...' : 'Register'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// Admin Panel Component (same as before - keeping for completeness)
function AdminPanel({ activeView }) {
  const [customers, setCustomers] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [allTransactions, setAllTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    if (activeView === 'customers') {
      loadCustomers();
    } else if (activeView === 'accounts') {
      loadAccounts();
    } else if (activeView === 'transactions') {
      loadAllTransactions();
    } else if (activeView === 'dashboard') {
      loadCustomers();
      loadAccounts();
    }
  }, [activeView]);

  const loadCustomers = async () => {
    setLoading(true);
    try {
      const response = await api.getAllCustomers();
      if (response.success) {
        setCustomers(response.data);
      }
    } catch (error) {
      console.error('Failed to load transactions:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadAccounts = async () => {
  setLoading(true);
  try {
    const response = await api.getAllAccounts();
    if (response.success) {
      setAccounts(response.data);
    }
  } catch (error) {
    console.error('Failed to load accounts:', error);
  } finally {
    setLoading(false);
  }
};

const loadAllTransactions = async () => {
  setLoading(true);
  try {
    const response = await api.getAllTransactions(); 
    if (response.success) {
      setAllTransactions(response.data);
    }
  } catch (error) {
    console.error('Failed to load transactions:', error);
  } finally {
    setLoading(false);
  }
};

  if (activeView === 'dashboard') {
    const totalBalance = accounts.reduce((sum, acc) => sum + (acc.balance || 0), 0);
    const activeAccounts = accounts.filter(acc => acc.status === 'Active').length;

    return (
      <div>
        <h2 className="text-3xl font-bold text-gray-900 mb-8">Admin Dashboard</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <StatCard icon={Users} label="Total Customers" value={customers.length} color="blue" />
          <StatCard icon={CreditCard} label="Total Accounts" value={accounts.length} color="green" />
          <StatCard icon={CreditCard} label="Active Accounts" value={activeAccounts} color="purple" />
          <StatCard icon={Wallet} label="Total Balance" value={`₹${totalBalance.toFixed(2)}`} color="orange" />
        </div>
      </div>
    );
  }

  if (activeView === 'customers') {
    const filteredCustomers = customers.filter(customer =>
      customer.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      customer.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      customer.phoneNumber.includes(searchTerm) ||
      customer.aadharNumber.includes(searchTerm)
    );

    return (
      <div>
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
          <h2 className="text-3xl font-bold text-gray-900">View Customers</h2>
          <div className="relative w-full sm:w-64">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
            <input
              type="text"
              placeholder="Search customers..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        {loading ? (
          <div className="text-center py-12">Loading...</div>
        ) : (
          <div className="bg-white rounded-xl shadow-lg overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">ID</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Name</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Email</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Phone</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Aadhar</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">DOB</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {filteredCustomers.map((customer) => (
                    <tr key={customer.customerId} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm text-gray-900">{customer.customerId}</td>
                      <td className="px-6 py-4 text-sm font-medium text-gray-900">{customer.name}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{customer.email}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{customer.phoneNumber}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{customer.aadharNumber}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{customer.dob ? new Date(customer.dob).toLocaleDateString() : 'N/A'}</td>
                      <td className="px-6 py-4">
                        <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                          customer.status === 'Active' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                        }`}>
                          {customer.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {filteredCustomers.length === 0 && (
              <div className="text-center py-8 text-gray-500">
                No customers found
              </div>
            )}
          </div>
        )}
      </div>
    );
  }

  if (activeView === 'accounts') {
    const filteredAccounts = accounts.filter(account =>
      account.accountNumber.includes(searchTerm) ||
      account.accountName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      account.bankName.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
      <div>
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
          <h2 className="text-3xl font-bold text-gray-900">View All Accounts</h2>
          <div className="relative w-full sm:w-64">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
            <input
              type="text"
              placeholder="Search accounts..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        {loading ? (
          <div className="text-center py-12">Loading...</div>
        ) : (
          <div className="bg-white rounded-xl shadow-lg overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Account No</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Name</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Type</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Balance</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Bank</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">IFSC</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {filteredAccounts.map((account) => (
                    <tr key={account.accountId} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm font-mono text-gray-900">{account.accountNumber}</td>
                      <td className="px-6 py-4 text-sm font-medium text-gray-900">{account.accountName}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{account.accountType}</td>
                      <td className="px-6 py-4 text-sm font-semibold text-green-600">₹{account.balance?.toFixed(2)}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">{account.bankName}</td>
                      <td className="px-6 py-4 text-sm font-mono text-gray-600">{account.ifscCode}</td>
                      <td className="px-6 py-4">
                        <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                          account.status === 'Active' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                        }`}>
                          {account.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {filteredAccounts.length === 0 && (
              <div className="text-center py-8 text-gray-500">
                No accounts found
              </div>
            )}
          </div>
        )}
      </div>
    );
  }

  if (activeView === 'transactions') {
    const filteredTransactions = allTransactions.filter(txn =>
      txn.transactionId.toLowerCase().includes(searchTerm.toLowerCase()) ||
      txn.senderAccountNumber?.includes(searchTerm) ||
      txn.receiverAccountNumber?.includes(searchTerm)
    );

    return (
      <div>
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
          <h2 className="text-3xl font-bold text-gray-900">View All Transactions</h2>
          <div className="relative w-full sm:w-64">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
            <input
              type="text"
              placeholder="Search transactions..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        {loading ? (
          <div className="text-center py-12">Loading transactions...</div>
        ) : (
          <div className="bg-white rounded-xl shadow-lg overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Date & Time</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Transaction ID</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Type</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Amount</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Mode</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">From</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">To</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {filteredTransactions.map((transaction) => (
                    <tr key={transaction.transactionId} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm text-gray-600">
                        {new Date(transaction.transactionTime).toLocaleString()}
                      </td>
                      <td className="px-6 py-4 text-sm font-mono text-gray-900">{transaction.transactionId}</td>
                      <td className="px-6 py-4">
                        <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                          transaction.transactionType === 'CREDITED' 
                            ? 'bg-green-100 text-green-800' 
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {transaction.transactionType}
                        </span>
                      </td>
                      <td className={`px-6 py-4 text-sm font-bold ${
                        transaction.transactionType === 'CREDITED' ? 'text-green-600' : 'text-red-600'
                      }`}>
                        ₹{transaction.transactionAmount?.toFixed(2)}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600">{transaction.transactionMode}</td>
                      <td className="px-6 py-4 text-sm text-gray-600 font-mono">{transaction.senderAccountNumber || 'N/A'}</td>
                      <td className="px-6 py-4 text-sm text-gray-600 font-mono">{transaction.receiverAccountNumber || 'N/A'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {filteredTransactions.length === 0 && (
              <div className="text-center py-8 text-gray-500">
                No transactions found
              </div>
            )}
          </div>
        )}
      </div>
    );
  }

  return <div>Select a view from the sidebar</div>;
}

// Customer Panel Component
function CustomerPanel({ activeView, currentUser }) {
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (currentUser?.accounts && currentUser.accounts.length > 0) {
      setSelectedAccount(currentUser.accounts[0]);
    }
  }, [currentUser]);

  useEffect(() => {
    if (activeView === 'transactions' && selectedAccount) {
      loadTransactions();
    }
  }, [activeView, selectedAccount]);

  const loadTransactions = async () => {
    if (!selectedAccount) return;
    setLoading(true);
    try {
      const response = await api.getTransactionsByAccount(selectedAccount.accountNumber);
      if (response.success) {
        setTransactions(response.data);
      }
    } catch (error) {
      console.error('Failed to load transactions:', error);
    } finally {
      setLoading(false);
    }
  };

  if (activeView === 'dashboard') {
    const totalBalance = currentUser?.accounts?.reduce((sum, acc) => sum + (acc.balance || 0), 0) || 0;

    return (
      <div>
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Welcome back, {currentUser?.name}!</h2>
        <p className="text-gray-600 mb-8">Here's your account overview</p>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <div className="bg-gradient-to-br from-blue-600 to-indigo-600 rounded-xl p-6 text-white shadow-xl">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h3 className="text-sm font-medium mb-1">Total Balance</h3>
                <p className="text-3xl font-bold">₹{totalBalance.toFixed(2)}</p>
              </div>
              <div className="bg-white/20 p-3 rounded-lg">
                <Wallet size={24} />
              </div>
            </div>
            <p className="text-sm text-blue-100">Across {currentUser?.accounts?.length || 0} accounts</p>
          </div>

          <div className="bg-gradient-to-br from-green-600 to-green-700 rounded-xl p-6 text-white shadow-xl">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h3 className="text-sm font-medium mb-1">Active Accounts</h3>
                <p className="text-3xl font-bold">{currentUser?.accounts?.filter(a => a.status === 'Active').length || 0}</p>
              </div>
              <div className="bg-white/20 p-3 rounded-lg">
                <CreditCard size={24} />
              </div>
            </div>
            <p className="text-sm text-green-100">All accounts operational</p>
          </div>
        </div>

        <h3 className="text-2xl font-bold text-gray-900 mb-4">Your Accounts</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {currentUser?.accounts?.map((account) => (
            <div key={account.accountId} className="bg-white rounded-xl p-6 shadow-lg border border-gray-200 hover:shadow-xl transition-shadow">
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h3 className="text-xl font-bold text-gray-900 mb-1">{account.accountType} Account</h3>
                  <p className="text-gray-500 text-sm">{account.bankName}</p>
                </div>
                <div className="bg-blue-100 p-3 rounded-lg">
                  <CreditCard className="text-blue-600" size={24} />
                </div>
              </div>
              
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-gray-600">Account Number</span>
                  <span className="font-mono font-semibold">{account.accountNumber}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">IFSC Code</span>
                  <span className="font-mono">{account.ifscCode}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-600">Balance</span>
                  <span className="text-2xl font-bold text-green-600">₹{account.balance?.toFixed(2)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Status</span>
                  <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                    account.status === 'Active' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                  }`}>
                    {account.status}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (activeView === 'profile') {
    return <EditProfileForm currentUser={currentUser} />;
  }

  if (activeView === 'accounts') {
    return (
      <div>
        <h2 className="text-3xl font-bold text-gray-900 mb-6">My Accounts</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {currentUser?.accounts?.map((account) => (
            <AccountCard key={account.accountId} account={account} currentUser={currentUser} />
          ))}
        </div>
      </div>
    );
  }

  if (activeView === 'createAccount') {
    return <CreateAccountForm currentUser={currentUser} />;
  }

  if (activeView === 'transfer') {
    return <TransferMoney currentUser={currentUser} />;
  }

  if (activeView === 'transactions') {
    return (
      <div>
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
          <h2 className="text-3xl font-bold text-gray-900">Transaction History</h2>
          <div className="flex items-center space-x-2">
            <select
              value={selectedAccount?.accountNumber || ''}
              onChange={(e) => {
                const account = currentUser?.accounts?.find(acc => acc.accountNumber === e.target.value);
                setSelectedAccount(account);
              }}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            >
              {currentUser?.accounts?.map((account) => (
                <option key={account.accountId} value={account.accountNumber}>
                  {account.accountNumber} - {account.accountType}
                </option>
              ))}
            </select>
          </div>
        </div>

        {loading ? (
          <div className="text-center py-12">Loading transactions...</div>
        ) : transactions.length === 0 ? (
          <div className="bg-white rounded-xl shadow-lg p-12 text-center">
            <ArrowLeftRight size={48} className="mx-auto text-gray-400 mb-4" />
            <p className="text-gray-600">No transactions found for this account</p>
          </div>
        ) : (
          <div className="bg-white rounded-xl shadow-lg overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Date & Time</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Transaction ID</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Type</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Amount</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Mode</th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-gray-600 uppercase">Details</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {transactions.map((transaction) => (
                    <tr key={transaction.transactionId} className="hover:bg-gray-50">
                      <td className="px-6 py-4 text-sm text-gray-600">
                        {new Date(transaction.transactionTime).toLocaleString()}
                      </td>
                      <td className="px-6 py-4 text-sm font-mono text-gray-900">{transaction.transactionId}</td>
                      <td className="px-6 py-4">
                        <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
                          transaction.transactionType === 'CREDITED' 
                            ? 'bg-green-100 text-green-800' 
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {transaction.transactionType}
                        </span>
                      </td>
                      <td className={`px-6 py-4 text-sm font-bold ${
                        transaction.transactionType === 'CREDITED' ? 'text-green-600' : 'text-red-600'
                      }`}>
                        {transaction.transactionType === 'CREDITED' ? '+' : '-'}₹{transaction.transactionAmount?.toFixed(2)}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600">{transaction.transactionMode}</td>
                      <td className="px-6 py-4 text-sm text-gray-600">
                        {transaction.transactionType === 'CREDITED' 
                          ? `From: ${transaction.senderAccountNumber}` 
                          : `To: ${transaction.receiverAccountNumber}`}
                        {transaction.description && <div className="text-xs text-gray-500 mt-1">{transaction.description}</div>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    );
  }

  return <div>Select a view from the sidebar</div>;
}

// CreateAccountForm Component
function CreateAccountForm({ currentUser }) {
  const [formData, setFormData] = useState({
    aadharNumber: currentUser?.aadharNumber || '',
    accountType: 'SAVINGS',
    accountName: currentUser?.name || '',
    accountNumber: '',
    phoneNumberLinked: currentUser?.phoneNumber || '',
    ifscCode: '',
    bankName: '',
    balance: '50.00'
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess(false);

    try {
      const response = await api.createAccount({
        ...formData,
        balance: parseFloat(formData.balance)
      });
      
      if (response.success) {
        setSuccess(true);
        setFormData({
          ...formData,
          accountNumber: '',
          ifscCode: '',
          bankName: '',
          balance: '50.00'
        });
        setTimeout(() => {
          window.location.reload();
        }, 2000);
      } else {
        setError(response.message || 'Failed to create account');
      }
    } catch (err) {
      setError('Failed to create account. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2 className="text-3xl font-bold text-gray-900 mb-6">Create New Account</h2>
      
      <div className="bg-white rounded-xl shadow-lg p-8 max-w-3xl">
        <form onSubmit={handleSubmit} className="space-y-6">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
              {error}
            </div>
          )}

          {success && (
            <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
              Account created successfully! Refreshing...
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Aadhar Number (Auto-filled)
              </label>
              <input
                type="text"
                value={formData.aadharNumber}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg bg-gray-50"
                disabled
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Account Type *
              </label>
              <select
                value={formData.accountType}
                onChange={(e) => setFormData({ ...formData, accountType: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                required
              >
                <option value="SAVINGS">Savings</option>
                <option value="CURRENT">Current</option>
                <option value="FIXED">Fixed Deposit</option>
                <option value="RECURRING">Recurring Deposit</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Account Name (Auto-filled)
              </label>
              <input
                type="text"
                value={formData.accountName}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg bg-gray-50"
                disabled
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Account Number * (10-18 digits)
              </label>
              <input
                type="text"
                value={formData.accountNumber}
                onChange={(e) => setFormData({ ...formData, accountNumber: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="1234567890123456"
                minLength="10"
                maxLength="18"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Linked Phone Number (Auto-filled)
              </label>
              <input
                type="tel"
                value={formData.phoneNumberLinked}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg bg-gray-50"
                disabled
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                IFSC Code *
              </label>
              <input
                type="text"
                value={formData.ifscCode}
                onChange={(e) => setFormData({ ...formData, ifscCode: e.target.value.toUpperCase() })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="SBIN0001234"
                maxLength="11"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Bank Name *
              </label>
              <input
                type="text"
                value={formData.bankName}
                onChange={(e) => setFormData({ ...formData, bankName: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="State Bank of India"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Initial Balance (₹) * (Min: ₹50)
              </label>
              <input
                type="number"
                step="0.01"
                value={formData.balance}
                onChange={(e) => setFormData({ ...formData, balance: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="50.00"
                min="50"
                required
              />
            </div>
          </div>

          <div className="flex justify-end space-x-4 pt-4">
            <button
              type="submit"
              disabled={loading || success}
              className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white px-8 py-3 rounded-lg font-semibold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Creating Account...' : 'Create Account'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// Transfer Money Component
function TransferMoney({ currentUser }) {
  const [formData, setFormData] = useState({
    senderAccountNumber: '',
    senderPin: '',
    receiverAccountNumber: '',
    amount: '',
    transactionMode: 'UPI',
    description: ''
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [showPin, setShowPin] = useState(false);

  useEffect(() => {
    if (currentUser?.accounts && currentUser.accounts.length > 0) {
      setFormData(prev => ({
        ...prev,
        senderAccountNumber: currentUser.accounts[0].accountNumber
      }));
    }
  }, [currentUser]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage({ type: '', text: '' });

    try {
      const response = await api.processTransaction({
        senderAccountNumber: formData.senderAccountNumber,
        senderPin: formData.senderPin,
        receiverAccountNumber: formData.receiverAccountNumber,
        amount: parseFloat(formData.amount),
        transactionMode: formData.transactionMode,
        description: formData.description
      });

      if (response.success) {
        setMessage({ type: 'success', text: 'Transaction successful! Email notifications have been sent to both parties.' });
        setFormData({
          ...formData,
          senderPin: '',
          receiverAccountNumber: '',
          amount: '',
          description: ''
        });
        // Refresh page after 3 seconds to update balance
        setTimeout(() => {
          window.location.reload();
        }, 3000);
      } else {
        setMessage({ type: 'error', text: response.message || 'Transaction failed' });
      }
    } catch (error) {
      setMessage({ type: 'error', text: 'Transaction failed. Please try again.' });
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const selectedAccount = currentUser?.accounts?.find(
    acc => acc.accountNumber === formData.senderAccountNumber
  );

  return (
    <div>
      <h2 className="text-3xl font-bold text-gray-900 mb-6">Transfer Money</h2>
      
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Transfer Form */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-xl shadow-lg p-6">
            <form onSubmit={handleSubmit} className="space-y-6">
              {message.text && (
                <div className={`px-4 py-3 rounded-lg ${
                  message.type === 'success' 
                    ? 'bg-green-50 border border-green-200 text-green-700' 
                    : 'bg-red-50 border border-red-200 text-red-700'
                }`}>
                  {message.text}
                </div>
              )}

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  From Account
                </label>
                <select
                  value={formData.senderAccountNumber}
                  onChange={(e) => setFormData({ ...formData, senderAccountNumber: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                >
                  {currentUser?.accounts?.map((account) => (
                    <option key={account.accountId} value={account.accountNumber}>
                      {account.accountNumber} - {account.accountType} (₹{account.balance?.toFixed(2)})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  To Account Number
                </label>
                <input
                  type="text"
                  value={formData.receiverAccountNumber}
                  onChange={(e) => setFormData({ ...formData, receiverAccountNumber: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  placeholder="Enter receiver account number"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Amount (₹)
                </label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.amount}
                  onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  placeholder="0.00"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Transaction Mode
                </label>
                <select
                  value={formData.transactionMode}
                  onChange={(e) => setFormData({ ...formData, transactionMode: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                >
                  <option value="UPI">UPI</option>
                  <option value="NEFT">NEFT</option>
                  <option value="IMPS">IMPS</option>
                  <option value="RTGS">RTGS</option>
                  <option value="TRANSFER">Transfer</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Description (Optional)
                </label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  placeholder="Add a note..."
                  rows="3"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Enter PIN *
                </label>
                <div className="relative">
                  <input
                    type={showPin ? 'text' : 'password'}
                    value={formData.senderPin}
                    onChange={(e) => setFormData({ ...formData, senderPin: e.target.value })}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    placeholder="Enter your 4-6 digit PIN"
                    maxLength="6"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPin(!showPin)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500"
                  >
                    {showPin ? <EyeOff size={20} /> : <Eye size={20} />}
                  </button>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-3 rounded-lg font-semibold hover:shadow-lg transform hover:scale-105 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
              >
                <Send size={20} />
                <span>{loading ? 'Processing...' : 'Transfer Money'}</span>
              </button>
            </form>
          </div>
        </div>

        {/* Account Summary */}
        <div className="space-y-6">
          {selectedAccount && (
            <div className="bg-gradient-to-br from-blue-600 to-indigo-600 rounded-xl p-6 text-white shadow-xl">
              <h3 className="text-lg font-semibold mb-4">Account Summary</h3>
              <div className="space-y-3">
                <div>
                  <p className="text-blue-100 text-sm">Available Balance</p>
                  <p className="text-3xl font-bold">₹{selectedAccount.balance?.toFixed(2)}</p>
                </div>
                <div className="border-t border-blue-400 pt-3 mt-3">
                  <p className="text-blue-100 text-sm mb-2">Account Details</p>
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-blue-100">Type</span>
                      <span className="font-semibold">{selectedAccount.accountType}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-blue-100">Bank</span>
                      <span className="font-semibold">{selectedAccount.bankName}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-blue-100">IFSC</span>
                      <span className="font-mono text-xs">{selectedAccount.ifscCode}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-6">
            <h3 className="font-semibold text-yellow-900 mb-3">Important Notes</h3>
            <ul className="space-y-2 text-sm text-yellow-800">
              <li className="flex items-start">
                <span className="mr-2">•</span>
                <span>Ensure sufficient balance before transfer</span>
              </li>
              <li className="flex items-start">
                <span className="mr-2">•</span>
                <span>Double-check receiver account number</span>
              </li>
              <li className="flex items-start">
                <span className="mr-2">•</span>
                <span>Email notifications will be sent to both parties</span>
              </li>
              <li className="flex items-start">
                <span className="mr-2">•</span>
                <span>Transaction cannot be reversed once completed</span>
              </li>
              <li className="flex items-start">
                <span className="mr-2">•</span>
                <span>Keep your PIN confidential and secure</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}

// Stat Card Component
function StatCard({ icon: Icon, label, value, color }) {
  const colorClasses = {
    blue: 'from-blue-500 to-blue-600',
    green: 'from-green-500 to-green-600',
    purple: 'from-purple-500 to-purple-600',
    orange: 'from-orange-500 to-orange-600'
  };

  return (
    <div className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-gray-600 text-sm mb-1">{label}</p>
          <p className="text-3xl font-bold text-gray-900">{value}</p>
        </div>
        <div className={`bg-gradient-to-br ${colorClasses[color]} p-4 rounded-lg`}>
          <Icon className="text-white" size={28} />
        </div>
      </div>
    </div>
  );
}

// Account Card Component with Edit
function AccountCard({ account, currentUser }) {
  const [isEditing, setIsEditing] = useState(false);

  if (isEditing) {
    return <EditAccountForm account={account} currentUser={currentUser} onCancel={() => setIsEditing(false)} />;
  }

  return (
    <div className="bg-white rounded-xl p-6 shadow-lg border border-gray-200">
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-xl font-bold text-gray-900 mb-1">{account.accountType}</h3>
          <p className="text-gray-500 text-sm">{account.bankName}</p>
        </div>
        <div className="bg-blue-100 p-3 rounded-lg">
          <CreditCard className="text-blue-600" size={24} />
        </div>
      </div>
      
      <div className="space-y-3">
        <div>
          <p className="text-gray-500 text-xs">Account Number</p>
          <p className="font-mono font-semibold">{account.accountNumber}</p>
        </div>
        <div>
          <p className="text-gray-500 text-xs">Account Name</p>
          <p className="font-semibold">{account.accountName}</p>
        </div>
        <div>
          <p className="text-gray-500 text-xs">IFSC Code</p>
          <p className="font-mono">{account.ifscCode}</p>
        </div>
        <div>
          <p className="text-gray-500 text-xs">Linked Phone</p>
          <p className="font-semibold">{account.phoneNumberLinked}</p>
        </div>
        <div className="border-t pt-3">
          <p className="text-gray-500 text-xs mb-1">Available Balance</p>
          <p className="text-3xl font-bold text-green-600">₹{account.balance?.toFixed(2)}</p>
        </div>
        <div className="flex justify-between items-center">
          <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
            account.status === 'Active' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
          }`}>
            {account.status}
          </span>
          <button
            onClick={() => setIsEditing(true)}
            className="text-blue-600 hover:text-blue-800 text-sm font-semibold"
          >
            Edit Details
          </button>
        </div>
      </div>
    </div>
  );
}

// Edit Profile Form Component
function EditProfileForm({ currentUser }) {
  const [formData, setFormData] = useState({
    name: currentUser?.name || '',
    phoneNumber: currentUser?.phoneNumber || '',
    email: currentUser?.email || '',
    address: currentUser?.address || '',
    customerPin: '',
    aadharNumber: currentUser?.aadharNumber || '',
    dob: currentUser?.dob || ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [showPin, setShowPin] = useState(false);

const handleSubmit = async (e) => {
  e.preventDefault();
  setLoading(true);
  setError('');
  setSuccess(false);

  try {
    // Clone formData
    const updatedData = { ...formData };

    // If PIN field is empty, don't include it in the request
    if (!updatedData.customerPin || updatedData.customerPin.trim() === '') {
      delete updatedData.customerPin;
    }

    const response = await api.updateCustomer(currentUser.customerId, updatedData);

    if (response.success) {
      setSuccess(true);
      setTimeout(() => {
        window.location.reload();
      }, 2000);
    } else {
      setError(response.message || 'Failed to update profile');
    }
  } catch (err) {
    setError('Failed to update profile. Please try again.');
    console.error(err);
  } finally {
    setLoading(false);
  }
};

  return (
    <div>
      <h2 className="text-3xl font-bold text-gray-900 mb-6">Edit My Profile</h2>
      
      <div className="bg-white rounded-xl shadow-lg p-8 max-w-4xl">
        <form onSubmit={handleSubmit} className="space-y-6">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
              {error}
            </div>
          )}

          {success && (
            <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
              Profile updated successfully! Refreshing...
            </div>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Full Name *
              </label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="John Doe"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Phone Number * (10 digits)
              </label>
              <input
                type="tel"
                value={formData.phoneNumber}
                onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="9876543210"
                maxLength="10"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Email Address *
              </label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                placeholder="john@example.com"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Aadhar Number (Cannot be changed)
              </label>
              <input
                type="text"
                value={formData.aadharNumber}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg bg-gray-50"
                disabled
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Date of Birth (Cannot be changed)
              </label>
              <input
                type="date"
                value={formData.dob ? new Date(formData.dob).toISOString().split('T')[0] : ''}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg bg-gray-50"
                disabled
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Update / Re-enter PIN 
              </label>
              <div className="relative">
                <input
                  type={showPin ? 'text' : 'password'}
                  value={formData.customerPin}
                  onChange={(e) => setFormData({ ...formData, customerPin: e.target.value })}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  placeholder="Enter new PIN (4-6 digits)"
                  maxLength="6"
                />
                <button
                  type="button"
                  onClick={() => setShowPin(!showPin)}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500"
                >
                  {showPin ? <EyeOff size={20} /> : <Eye size={20} />}
                </button>
              </div>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Address
            </label>
            <textarea
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              placeholder="Enter complete address"
              rows="3"
            />
          </div>

          <div className="flex justify-end space-x-4 pt-4">
            <button
              type="submit"
              disabled={loading || success}
              className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white px-8 py-3 rounded-lg font-semibold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Updating...' : 'Update Profile'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// Edit Account Form Component
function EditAccountForm({ account, currentUser, onCancel }) {
  const [formData, setFormData] = useState({
    accountType: account.accountType || '',
    accountName: account.accountName || '',
    phoneNumberLinked: account.phoneNumberLinked || '',
    status: account.status || ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess(false);

    try {
      const response = await api.updateAccount(account.accountNumber, formData);
      
      if (response.success) {
        setSuccess(true);
        setTimeout(() => {
          window.location.reload();
        }, 2000);
      } else {
        setError(response.message || 'Failed to update account');
      }
    } catch (err) {
      setError('Failed to update account. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white rounded-xl p-6 shadow-lg border border-gray-200">
      <h3 className="text-xl font-bold text-gray-900 mb-4">Edit Account Details</h3>
      
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
            {error}
          </div>
        )}

        {success && (
          <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg text-sm">
            Account updated successfully! Refreshing...
          </div>
        )}

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Account Number (Cannot be changed)
          </label>
          <input
            type="text"
            value={account.accountNumber}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50 text-sm"
            disabled
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Account Type *
          </label>
          <select
            value={formData.accountType}
            onChange={(e) => setFormData({ ...formData, accountType: e.target.value })}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
            required
          >
            <option value="SAVINGS">Savings</option>
            <option value="CURRENT">Current</option>
            <option value="FIXED">Fixed Deposit</option>
            <option value="RECURRING">Recurring Deposit</option>
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Account Name *
          </label>
          <input
            type="text"
            value={formData.accountName}
            onChange={(e) => setFormData({ ...formData, accountName: e.target.value })}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
            placeholder="Account holder name"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Linked Phone Number * (10 digits)
          </label>
          <input
            type="tel"
            value={formData.phoneNumberLinked}
            onChange={(e) => setFormData({ ...formData, phoneNumberLinked: e.target.value })}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
            placeholder="9876543210"
            maxLength="10"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Account Status *
          </label>
          <select
            value={formData.status}
            onChange={(e) => setFormData({ ...formData, status: e.target.value })}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
            required
          >
            <option value="Active">Active</option>
            <option value="Inactive">Inactive</option>
            <option value="BLOCKED">Blocked</option>
            <option value="SUSPENDED">Suspended</option>
          </select>
        </div>

        <div className="bg-gray-50 p-3 rounded-lg">
          <p className="text-xs text-gray-600">
            <strong>Note:</strong> Balance, IFSC Code, and Bank Name cannot be changed once created.
          </p>
        </div>

        <div className="flex space-x-3 pt-2">
          <button
            type="button"
            onClick={onCancel}
            className="flex-1 bg-gray-200 text-gray-800 py-2 rounded-lg font-semibold hover:bg-gray-300 transition-colors text-sm"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading || success}
            className="flex-1 bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-2 rounded-lg font-semibold hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed text-sm"
          >
            {loading ? 'Updating...' : 'Update Account'}
          </button>
        </div>
      </form>
    </div>
  );
}